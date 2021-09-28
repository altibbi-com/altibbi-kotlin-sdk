# AltibbiSdk

## Example

To run the example project, clone the repo, and run `pod install` from the Example directory first.

## Init

```ruby
You must add API URL and TOKEN in LOGIN setp

link => ADD the api link
token => add the TOKEN
```

## Installation

AltibbiSdk is available through [CocoaPods](https://cocoapods.org). To install
it, simply add the following line to your Podfile:

```ruby
pod 'AltibbiSdk'
```

## User

```ruby
id: user ID
completionHandler: To handle the resposne

AltibbiSdk.getUserInfo(id: String, completionHandler: @escaping (Data?, Error?) -> Void)
```
```ruby
id: user ID
data : [
    "name": "userName",
    "gender": "male" or "female"
]
completionHandler: To handle the resposne

AltibbiSdk.updateUserInfo(id: String, data: Dictionary<String, Any>, completionHandler: @escaping (Data?, Error?) -> Void)
```
```ruby
completionHandler: To handle the resposne

AltibbiSdk.getPhrList(completionHandler: @escaping (Data?, Error?) -> Void)
```

## Consultation

```ruby
completionHandler: To handle the resposne

AltibbiSdk.getConsultationsList (completionHandler: @escaping (Data?, Error?) -> Void)
```
```ruby
consultationId : consultation ID
completionHandler: To handle the resposne

AltibbiSdk.getConsultationInfo (consultationId: Int, completionHandler: @escaping (Data?, Error?) -> Void)
```
```ruby
data : [
    "user_id": user ID,
    "question": Question Title,
    "medium": chat,
]
completionHandler: To handle the resposne

AltibbiSdk.createConsultation (data: Dictionary<String, Any>, completionHandler: @escaping (Data?, Error?) -> Void);
``` 
```ruby
consultationId : consultation ID
completionHandler: To handle the resposne

AltibbiSdk.cancelConsultation (consultationId: Int, completionHandler: @escaping (Data?, Error?) -> Void)
```
```ruby
consultationId : consultation ID
completionHandler: To handle the resposne

AltibbiSdk.deleteConsultation (consultationId: Int, completionHandler: @escaping (Data?, Error?) -> Void)
```

## Handle Response

```ruby
completionHandler: (Data?, Error?) -> Void

For example, a complete call of getUserInfo with completion handler to print the response could be something like this:

AltibbiSdk.getUserInfo(id: "000", completionHandler: {Data, Error in
    if let Data = Data {
        print(String(data: Data, encoding: .utf8)!)
    }
})
```

## Author

Mahmoud Johar, mahmoud.abojoher@altibbi.com
