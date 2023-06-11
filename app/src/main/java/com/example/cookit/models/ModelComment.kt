package com.example.cookit.models

class ModelComment {

    var id = ""
    var recipeId = ""
    var timestamp = ""
    var comment = ""
    var uid = ""

    constructor()

    constructor(id: String, recipeId: String, timestamp: String, comment: String, uid: String) {
        this.id = id
        this.recipeId = recipeId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }


}