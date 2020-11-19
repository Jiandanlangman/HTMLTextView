package com.jiandanlangman.htmltextview

internal object Util {

     fun <T> tryCatchInvoke(runnable: () -> T, errorReturn: T): T {
        return try {
            runnable.invoke()
        } catch (ignore: Throwable) {
            errorReturn
        }
    }

    fun tryCatchInvoke(runnable:() -> Unit) {
        try {
            runnable.invoke()
        } catch (ignore:Throwable) {

        }
    }

    fun dpToPx(dp:Float, density:Float) = (dp * density + .5f).toInt()

    fun dpToPx(dp:Int, density:Float) = Util.dpToPx(dp.toFloat(), density)

}