# FoodLens

<!-- [![CI Status](https://img.shields.io/travis/hyunsuk.lee@doinglab.com/FoodLens.svg?style=flat)](https://travis-ci.org/hyunsuk.lee@doinglab.com/FoodLens)
[![Version](https://img.shields.io/cocoapods/v/FoodLens.svg?style=flat)](https://cocoapods.org/pods/FoodLens)
[![License](https://img.shields.io/cocoapods/l/FoodLens.svg?style=flat)](https://cocoapods.org/pods/FoodLens)
[![Platform](https://img.shields.io/cocoapods/p/FoodLens.svg?style=flat)](https://cocoapods.org/pods/FoodLens) -->

## Requirements

* iOS Ver 10.0 or higher
* Swift Version 4.2 or higher

## FoodLens SDK V2 (Ver. 2.0.27)
![](V201?raw=true)
![](V202?raw=true)

## FoodLens SDK V1 (Ver. 0.1.15)
![](V101?raw=true)
![](V102?raw=true)


## Example  
[Sample](SampleCode/)

## Installation

First, add repository using  `pod repo add`

```ruby
pod repo add bitbucket-doing-lab-foodlenssdk-specs https://bitbucket.org/doing-lab/foodlenssdk-specs.git
```
When credentials are needed, we have given you some information to certify, please use it.

Next, add source clause of FoodLens into your Podfile.  

```ruby
source 'https://bitbucket.org/doing-lab/foodlenssdk-specs.git'
```

When you use other library (Alamofire, Kingfisher etc...), you must add cocoapod source into your Podfile. 

```ruby
source 'https://bitbucket.org/doing-lab/foodlenssdk-specs.git'
source 'https://github.com/CocoaPods/Specs.git'
```

And add below into your Podfile

```ruby
pod 'FoodLens'
```

## Using FoodLens UI

You can use UI served by default
(As of 2.0.27, the UI is displayed in light mode.)

```swift
FoodLens.uiServiceMode = .userSelectedWithCandidates

let uiService = FoodLens.createUIService(accessToken: "<Access Token Here>") //AccessToken is given to you
uiService?.startUIService(parent: self, completionHandler: self)
```
A completionHandler is protocol called when recognition process is completed

```swift
public protocol UserServiceResultHandler {
    func onSuccess(_ result : RecognitionResult)    //called when process is succeeded
    func onCancel()                                 //called when user cancels recognition
    func onError(_ error : BaseError)               //called when error is occurred
}
```
When you want to modify recognition result, you can use editing service 

```swift
let mealData = PredictionResult()    // PredictionResult implements RecognitionResult protocol
let foodPosition = FoodPosition()
let food = Food()
food.foodName = "FoodName"
let nutrition = Nutrition()
nutrition.calories = 5000
food.nutrition = nutrition
foodPosition.foodCandidates.append(food)
foodPosition.userSelectedFood = foodPosition.foodCandidates[0]
mealData.putFoodPosition(foodPosition)

FoodLens.uiServiceMode = .userSelectedWithCandidates

let uiService = FoodLens.createUIService(accessToken: "<Access Token Here>") //AccessToken is given to you
uiService.startEditUIService(mealData, parent: self, completionHandler: CallbackObject())    
```
A completionHandler is protocol called when recognition process is completed

You can also apply custom theme using like below

```swift
let navTheme = NavigationBarTheme(foregroundColor : UIColor.white, backgroundColor : UIColor.black)
let toolbarTheme = ToolBarButtonTheme(backgroundColor: UIColor.white, buttonTheme: ButtonTheme(backgroundColor: UIColor.black, textColor: UIColor.white, borderColor: UIColor.clear))
let buttonTheme = ButtonTheme(backgroundColor: UIColor.blue, textColor: UIColor.green, borderColor: UIColor.black)
let widgetButtonTheme = ButtonTheme(backgroundColor: UIColor.black, textColor: UIColor.blue, borderColor: UIColor.red)
       
let uiService = FoodLens.createUIService(accessToken:  "<Access Token Here>", navigationBarTheme: navTheme, toolbarTheme: toolbarTheme, buttonTheme: buttonTheme,  widgetButtonTheme : widgetButtonTheme)
FoodLens.uiServiceMode = .userSelectedWithCandidates
uiService.startUIService(parent: self, completionHandler: CallbackObject())   
```

## Working with JSON 

You can convert result Object to JSON String like below in UserServiceResultHandler.onSuccess

```swift

func onSuccess(_ result : RecognitionResult) {
    let resultString = result.toJSONString()! //will return JSON string
    print(resultString)
}
```

When you convert JSON String to Result Object, use create method like below 

```swift
    let predictResult = PredictionResult.create(json: jsonString)
```

## Using Only Network API

Of cource you can use only network API like this.

```swift
let networkService = FoodLens.createNetworkService(nutritionRetrieveMode: .allNutirition, accessToken: "<Access Token Here>") //AccessToken is given to you
networkService!.predictMultipleFood(image: pickedImage) { (result : PredictionResult?, status : ProcessStatus) in
    
}
```
PredictionResult is the object that implements RecognitionResult protocol

## Documents  
[API Documents](https://doinglab.github.io/ios/index.html)

## Author

hyunsuk.lee@doinglab.com

## License

FoodLens is available under the MIT license. See the LICENSE file for more info.
