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
        FoodLens.uiServiceMode = .userSelectedWithCandidates
        uiService = FoodLens.createUIService(accessToken: "<access Token here>")
        networkService = FoodLens.createNetworkService(nutritionRetrieveMode: .allNutirition, accessToken: "<access Token here>")
    }
    
    @IBAction func startUIService(_ sender: Any) {
        statusLabel.text = ""
        Util.deleteDirectory(path : "foodlensStore")
        uiService?.startUIService(parent: self, completionHandler: self)
    }
    
    func saveImage() -> String? {
        let path:String = "foodlensStore";
        let image = UIImage(named: "aa2c2043c2984529b77851d25c7bbf6c.jpg")
        let filename = "aa2c2043c2984529b77851d25c7bbf6c.jpg"
        
        let documentsUrl =  FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        
        var targetUrl:URL! = documentsUrl
        
        targetUrl = documentsUrl.appendingPathComponent(path)
        
        let fileURL = targetUrl.appendingPathComponent(filename)
        
        if let imageData = image?.jpegData(compressionQuality: 1.0) {
            do {
                try FileManager.default.createDirectory(atPath: targetUrl.path, withIntermediateDirectories: true, attributes: nil)
                try imageData.write(to: fileURL, options: .atomic)
                return fileURL.absoluteString // ----> Save fileName
            }
            catch {
                print("Error saving image : \(error)")
            }
        }
        
        return nil
    }
    
    @IBAction func startEditServiceWithDummy(_ sender: Any) {
        let mealData = PredictionResult()
        
        mealData.setRecognizedImagePath(saveImage()!)
        
        var foodPosition = FoodPosition()
        var food = Food()
        food.foodName = "아욱된장국"
        var nutrition = Nutrition()
        nutrition.calories = 5000
        food.nutrition = nutrition
        foodPosition.imagePosition = Box()
        foodPosition.imagePosition?.xmin = 568
        foodPosition.imagePosition?.xmax = 1017
        foodPosition.imagePosition?.ymin = 442
        foodPosition.imagePosition?.ymax = 898
        foodPosition.foodCandidates.append(food)
        foodPosition.userSelectedFood = foodPosition.foodCandidates[0]
        mealData.putFoodPosition(foodPosition)
        
        foodPosition = FoodPosition()
        food = Food()
        food.foodName = "깍두기"
        nutrition = Nutrition()
        nutrition.calories = 5000
        food.nutrition = nutrition
        foodPosition.imagePosition = Box()
        foodPosition.imagePosition?.xmin = 758
        foodPosition.imagePosition?.xmax = 1018
        foodPosition.imagePosition?.ymin = 74
        foodPosition.imagePosition?.ymax = 345
        foodPosition.foodCandidates.append(food)
        foodPosition.userSelectedFood = foodPosition.foodCandidates[0]
        mealData.putFoodPosition(foodPosition)
        
        foodPosition = FoodPosition()
        food = Food()
        food.foodName = "고등어조림"
        nutrition = Nutrition()
        nutrition.calories = 5000
        food.nutrition = nutrition
        foodPosition.imagePosition = Box()
        foodPosition.imagePosition?.xmin = 400
        foodPosition.imagePosition?.xmax = 678
        foodPosition.imagePosition?.ymin = 177
        foodPosition.imagePosition?.ymax = 411
        foodPosition.foodCandidates.append(food)
        foodPosition.userSelectedFood = foodPosition.foodCandidates[0]
        mealData.putFoodPosition(foodPosition)
        
        let navTheme = NavigationBarTheme(foregroundColor : UIColor.black, backgroundColor : UIColor.blue)
        let toolbarTheme = ToolBarButtonTheme(backgroundColor: UIColor.brown, buttonTheme: ButtonTheme(backgroundColor: UIColor.cyan, textColor: UIColor.darkGray, borderColor: UIColor.gray))
        let buttonTheme = ButtonTheme(backgroundColor: UIColor.green, textColor: UIColor.lightGray, borderColor: UIColor.magenta)
        let widgetButtonTheme = ButtonTheme(backgroundColor: UIColor.orange, textColor: UIColor.purple, borderColor: UIColor.red)
        
        let uiService = FoodLens.createUIService(accessToken:  "<access Token here>", navigationBarTheme: navTheme, toolbarTheme: toolbarTheme, buttonTheme: buttonTheme,  widgetButtonTheme : widgetButtonTheme)
        uiService.startEditUIService(mealData, parent: self, completionHandler: self)
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

