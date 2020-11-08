# iOS FoodLens SDK 메뉴얼

<!-- [![CI Status](https://img.shields.io/travis/hyunsuk.lee@doinglab.com/FoodLens.svg?style=flat)](https://travis-ci.org/hyunsuk.lee@doinglab.com/FoodLens)
[![Version](https://img.shields.io/cocoapods/v/FoodLens.svg?style=flat)](https://cocoapods.org/pods/FoodLens)
[![License](https://img.shields.io/cocoapods/l/FoodLens.svg?style=flat)](https://cocoapods.org/pods/FoodLens)
[![Platform](https://img.shields.io/cocoapods/p/FoodLens.svg?style=flat)](https://cocoapods.org/pods/FoodLens) -->

## Requirements

* iOS Ver 11.0 이상
* Swift Version 4.2 이상

## FoodLens SDK V2 (Ver. 2.1.9)
<img src="./Images/V201.PNG" width="150" height="300">      <img src="./Images/V202.PNG" width="150" height="300">

## FoodLens SDK V1 (Ver. 0.1.15) - Decrecate V1버전은 더이상 기술 지원을 하지 않습니다.
<img src="./Images/V101.PNG" width="150" height="300">      <img src="./Images/V102.PNG" width="150" height="300">


## Installation

`pod repo add` 명령어를 사용하여 리포지토리를 추가합니다. 

```ruby
pod repo add bitbucket-doing-lab-foodlenssdk-specs https://bitbucket.org/doing-lab/foodlenssdk-specs.git
```
Credential 정보를 입력하라는 화면이 나오면, 전달받은 인증 정보를 입력합니다.

Podfile 에 아래와 같은 행을 추가합니다.

```ruby
source 'https://bitbucket.org/doing-lab/foodlenssdk-specs.git'
```

Alamofire나 Kingfisher 같은 CocoaPod 라이브러리를 사용할 경우, 아래와 같이 CocoaPod 의 Source 도 같이 추가해야 합니다. 

```ruby
source 'https://bitbucket.org/doing-lab/foodlenssdk-specs.git'
source 'https://github.com/CocoaPods/Specs.git'
```

Podfile 에 아래와 같은 구문을 추가하여 FoodLens 를 import 합니다.

```ruby
pod 'FoodLens', '2.1.9'
```
** [ReleaseNote](ReleaseNote.md)

## Using FoodLens UI

FoodLens 에서 제공하는 UI 를 아래와 같이 사용할 수 있습니다.
(2.0.27 버전부터는 Light Mode 로만 UI 가 표시됩니다.)


```swift
FoodLens.uiServiceMode = .userSelectedWithCandidates

let uiService = FoodLens.createUIService(accessToken: "<Access Token Here>") //AccessToken is given to you
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
