package meow.utils

import greycat.struct.LongLongArrayMap


object GreyCatUtilitary{

    fun keyOfLongToLongArrayMap(llamap :LongLongArrayMap):LongArray{
        val listOfKeys = mutableListOf<Long>()
        llamap.each { key, value ->
            listOfKeys.add(key)
        }
        return listOfKeys.toLongArray()
    }
}