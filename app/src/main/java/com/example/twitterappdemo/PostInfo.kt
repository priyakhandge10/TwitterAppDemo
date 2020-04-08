package com.example.twitterappdemo

class PostInfo {
    var userUID:String?=null
    var text:String?=null
    var postImage:String?=null
    constructor(userUID:String, text:String, postImage:String){
        this.userUID=userUID
        this.text=text
        this.postImage=postImage

    }
}