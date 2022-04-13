# iOS FoodLens SDK 메뉴얼

<!-- [![CI Status](https://img.shields.io/travis/hyunsuk.lee@doinglab.com/FoodLens.svg?style=flat)](https://travis-ci.org/hyunsuk.lee@doinglab.com/FoodLens)
[![Version](https://img.shields.io/cocoapods/v/FoodLens.svg?style=flat)](https://cocoapods.org/pods/FoodLens)
[![License](https://img.shields.io/cocoapods/l/FoodLens.svg?style=flat)](https://cocoapods.org/pods/FoodLens)
[![Platform](https://img.shields.io/cocoapods/p/FoodLens.svg?style=flat)](https://cocoapods.org/pods/FoodLens) -->

## [ReleaseNote 바로가기](ReleaseNote.md)

## Requirements

* iOS Ver 12.0 이상
* Swift Version 4.2 이상
* 2.4.1 버전부터 private repository가 아닌 cocoapod public repository에 릴리즈 됩니다.

## FoodLens SDK V2 (Ver. 2.4.3)
<img src="./Images/V201.PNG" width="150" height="300">      <img src="./Images/V202.PNG" width="150" height="300">



## Installation

Podfile 에 아래와 같은 구문을 추가하여 FoodLens 를 import 합니다.

```ruby
platform :ios, '12.0'

target 'FoodLensApp' do
  # Comment the next line if you're not using Swift and don't want to use dynamic frameworks
  use_frameworks!
  pod 'FoodLens', '2.4.3'
  ....
```

pod install시 Foodlens 검색 안될시 repository 업데이트와 함께 설치
```
pod install --repo-update
```

기존 private repository를 사용했던 사용자는 아래 커맨드를 활용하여 기존 foodlens private repo를 삭제 후 업데이트
```
pod repo remove [repo name]
```


## Using FoodLens UI

FoodLens 에서 제공하는 UI 를 아래와 같이 사용할 수 있습니다.
(2.0.27 버전부터는 Light Mode 로만 UI 가 표시됩니다.)


```swift
FoodLens.uiServiceMode = .userSelectedWithCandidates

//NOTE AccessToken만 있는 경우
let uiService = FoodLens.createUIService(accessToken: "<Access Token Here>")

//NOTE AppToken, CompanyToken모두 있는 경우
let uiService = FoodLens.createUIService(appToken: "<App Token Here>", companyToken: "<Company Token Here>")

uiService?.startUIService(parent: self, completionHandler: self)
```
completionHandler 는 callback 을 받을 swift protocol 이며, 아래와 같이 정의되어 있습니다.

```swift
public protocol UserServiceResultHandler {
    func onSuccess(_ result : RecognitionResult)    //called when process is succeeded
    func onCancel()                                 //called when user cancels recognition
    func onError(_ error : BaseError)               //called when error is occurred
}
```

음식 인식 결과를 수정해야 할 경우, 아래와 같이 사용하실 수 있습니다.  

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
completionHandler 는 callback 을 받을 swift protocol 입니다.

FoodLens UI 의 여러 요소에 개별 색을 적용할 수 있습니다. 

```swift
let navTheme = NavigationBarTheme(foregroundColor : UIColor.white, backgroundColor : UIColor.black)
let toolbarTheme = ToolBarButtonTheme(backgroundColor: UIColor.white, buttonTheme: ButtonTheme(backgroundColor: UIColor.black, textColor: UIColor.white, borderColor: UIColor.clear))
let buttonTheme = ButtonTheme(backgroundColor: UIColor.blue, textColor: UIColor.green, borderColor: UIColor.black)
let widgetButtonTheme = ButtonTheme(backgroundColor: UIColor.black, textColor: UIColor.blue, borderColor: UIColor.red)
       
let uiService = FoodLens.createUIService(accessToken:  "<Access Token Here>", navigationBarTheme: navTheme, toolbarTheme: toolbarTheme, buttonTheme: buttonTheme,  widgetButtonTheme : widgetButtonTheme)
FoodLens.uiServiceMode = .userSelectedWithCandidates
uiService.startUIService(parent: self, completionHandler: CallbackObject())   
```

## Using Only Network API

FoodLens UI 가 필요없는 경우, 아래 함수만 호출하여 음식 인식 결과를 받을 수 있습니다.

```swift
let networkService = FoodLens.createNetworkService(nutritionRetrieveMode: .allNutirition, accessToken: "<Access Token Here>") //AccessToken is given to you
networkService!.predictMultipleFood(image: pickedImage) { (result : PredictionResult?, status : ProcessStatus) in
    
}
```
## FoodLens 독립 서버 주소 설정

기본 FoodLens 서버가 아닌 독립 서버를 운용할 경우 서버 주소를 설정 할 수 있습니다.
```swift
//info.plist에 FoodLensServerAddr 항목을 추가하고 서버 주소를 추가
//도메인 이름만 추가 http, https등 프로토콜은 추가하지 않음 e.g.) www.domain.com, 132.213.111.23 등
```
<img src="./Images/infoplist.png">

## Working with JSON 

UserServiceResultHandler.onSuccess 함수의 파라미터로 전달되는 RecognitionResult 객체를 JSON 문자열로 변환할 수 있습니다. 

```swift

func onSuccess(_ result : RecognitionResult) {
    let resultString = result.toJSONString()! //will return JSON string
    print(resultString)
}
```
 
JSON 문자열을 PredictionResult 객체로 변환할 경우, 아래처럼 사용하실 수 있습니다.

```swift
    let predictResult = PredictionResult.create(json: jsonString)
```
PredictionResult 은 RecognitionResult protocol 의 구현체 입니다.

## Eat amount info

```swift
    for index in 0 ..< result.foodPositionList.count {
        let eatAmount = result.foodPositionList[index].eatAmount
        let nutrition = result.foodPositionList[index].userSelectedFood?.nutrition
        eatAmount * nutrition?.carbonhydrate // // 1회 섭취한 음식에 대한 탄수화물 섭취량
        eatAmount * nutrition?.protein // // 1회 섭취한 음식에 대한 단백질 섭취량
        eatAmount * nutrition?.fat // // 1회 섭취한 음식에 대한 지방 섭취량
        ...
    }
```

## Documents  
[API Documents](https://doinglab.github.io/ios/index.html)

## Example  
[Sample](SampleCode/)

## JSON Format
[JSON Format](../JSON%20Format)

[JSON Sample](../JSON%20Sample)

## Author
hyunsuk.lee@doinglab.com

## License
FoodLens is available under the MIT license. See the LICENSE file for more info.
