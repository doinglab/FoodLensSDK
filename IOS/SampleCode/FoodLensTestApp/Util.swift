//
//  Util.swift
//  FoodLensTestApp
//
//  Created by HwangChun Lee on 02/04/2019.
//  Copyright © 2019 DoingLab Inc. All rights reserved.
//

import Foundation
import UIKit

class Util {
    static func save(image: UIImage, path:String?, fileName:String) -> String? {
        let documentsUrl =  FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        
        var targetUrl:URL! = documentsUrl
        if path != nil {
            targetUrl = documentsUrl.appendingPathComponent(path!)
        }
        
        let fileURL = targetUrl.appendingPathComponent(fileName)
        
        
        if let imageData = image.jpegData(compressionQuality: 1.0) {
            do {
                try FileManager.default.createDirectory(atPath: targetUrl.path, withIntermediateDirectories: true, attributes: nil)
                try imageData.write(to: fileURL, options: .atomic)
                return fileURL.absoluteString // ----> Save fileName
            }
            catch {
                print("Error saving image : \(error)")
            }
        }
        //print("Error saving image")
        return nil
    }
    
    static func load(path:String?, fileName:String) -> UIImage? {
        let documentsUrl =  FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        var targetUrl:URL! = documentsUrl
        if path != nil {
            targetUrl = documentsUrl.appendingPathComponent(path!)
        }
        
        let fileURL = targetUrl.appendingPathComponent(fileName)
        do {
            let imageData = try Data(contentsOf: fileURL)
            return UIImage(data: imageData)
        } catch {
            print("Error loading image : \(error)")
        }
        return nil
    }
    
    static func deleteDirectory(path : String?) {
        let documentsUrl =  FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        var dataPath:URL
        if path != nil {
            dataPath = documentsUrl.appendingPathComponent(path!)
        } else {
            dataPath = documentsUrl
        }
        
        do {
            try FileManager.default.removeItem(atPath: dataPath.path)
        } catch {
            print("Could not clear temp folder: \(error)")
        }
    }
}
