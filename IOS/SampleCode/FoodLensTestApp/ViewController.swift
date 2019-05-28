//
//  ViewController.swift
//  FoodLensTestApp
//
//  Created by HwangChun Lee on 29/03/2019.
//  Copyright © 2019 DoingLab Inc. All rights reserved.
//

import UIKit
import FoodLens

class ViewController: UIViewController, UserServiceResultHandler, UIImagePickerControllerDelegate, UINavigationControllerDelegate, UITableViewDelegate, UITableViewDataSource {
    @IBOutlet weak var statusLabel: UILabel!
    var result : RecognitionResult? = nil
    @IBOutlet weak var tableView: UITableView!
    
    func onSuccess(_ result: RecognitionResult) {
        self.result = result
        tableView.reloadData()
        NSLog("Success")
    }
    
    func onCancel() {
        NSLog("Canceled")
    }
    
    func onError(_ error: BaseError) {
        NSLog("onError")
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 80.0
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let result = self.result {
            return result.getFoodPositions().count+1 ?? 0
        }
        else {
            return 0
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "TableViewCell", for: indexPath) as! TableViewCell
        if indexPath.row == 0 {
            if let imagePath = result?.getRecognizedImage()?.split(separator: "/").last {
                cell.foodImage.image = Util.load(path: "foodlensStore", fileName: String(imagePath))
            }
            cell.foodName.text = "full image"
            cell.foodUnit.text = ""
        }
        else {
            if let data = result?.getFoodPositionAtIndex(index: indexPath.row-1) {
                if let imageFilename = data.foodImagepath?.split(separator: "/").last {
                    cell.foodImage.image = Util.load(path: "foodlensStore", fileName: String(imageFilename))
                }
                
                if let foodname = data.userSelectedFood?.foodName {
                    cell.foodName.text = data.userSelectedFood?.foodName ?? ""
                }
                else {
                    cell.foodName.text = data.foodCandidates[0].foodName
                }
                
                if let calorie = data.userSelectedFood?.nutrition?.calories {
                    cell.foodUnit.text = "\(calorie) Kcal"
                }
                else if let calorie = data.foodCandidates[0].nutrition?.calories {
                    cell.foodUnit.text = "\(calorie) Kcal"
                }
                
            }
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        startEditUIService()
    }
    
    var uiService : UIService? = nil
    var networkService : NetworkService? = nil

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        let navTheme = NavigationBarTheme(foregroundColor : UIColor.white, backgroundColor : UIColor.black)
        let toolbarTheme = ToolBarButtonTheme(backgroundColor: UIColor.white, buttonTheme: ButtonTheme(backgroundColor: UIColor.black, textColor: UIColor.white, borderColor: UIColor.clear))
        let buttonTheme = ButtonTheme(backgroundColor: UIColor.white, textColor: UIColor.red, borderColor: UIColor.black)
        let widgetButtonTheme = ButtonTheme(backgroundColor: UIColor.black, textColor: UIColor.blue, borderColor: UIColor.clear)
        
        FoodLens.uiServiceMode = .userSelectedWithCandidates
        uiService = FoodLens.createUIService(accessToken: "insert access token here", navigationBarTheme: navTheme, toolbarTheme: toolbarTheme, buttonTheme: buttonTheme,  widgetButtonTheme : widgetButtonTheme)
        networkService = FoodLens.createNetworkService(nutritionRetrieveMode: .allNutirition, accessToken: "insert access token here")
    }
    
    @IBAction func startUIService(_ sender: Any) {
        statusLabel.text = ""
        Util.deleteDirectory(path : "foodlensStore")
        
        uiService?.startUIService(parent: self, completionHandler: self)
    }
    
    func startEditUIService() {
        guard let result = result else {
            return
        }
        
        let mealData = PredictionResult()
        mealData.setRecognizedImagePath(result.getRecognizedImage()!)
        for elem in result.getFoodPositions() {
            mealData.putFoodPosition(elem)
        }
        uiService?.startEditUIService(mealData, parent: self, completionHandler: self)
    }
    
    @IBAction func startNetworkService(_ sender: Any) {
        statusLabel.text = ""
        Util.deleteDirectory(path : "foodlensStore")
        let imagePicker = UIImagePickerController()
        imagePicker.delegate = self
        
        imagePicker.allowsEditing = false
        imagePicker.sourceType = .photoLibrary
        
        present(imagePicker, animated: true, completion: nil)
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        dismiss(animated: true, completion: nil)
        if let pickedImage = info[.originalImage] as? UIImage,
        let networkService = self.networkService {
            statusLabel.text = "wait for response..."
            networkService.predictMultipleFood(image: pickedImage) { (result : PredictionResult?, status : ProcessStatus) in
                self.statusLabel.text = "completed"
                self.result = result
                self.tableView.reloadData()
            }
        }
    }
}

