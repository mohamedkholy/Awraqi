package com.dev3mk.awraqi.data.model

data class Version(val version:String,val mandatory:Boolean){
    constructor():this("",false)
}