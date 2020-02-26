import com.theapache64.automotion.utils.SimpleCommandExecutor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val REGEX = "(?<file>.+):.+Created\\sby\\s(?<name>.+)\\son\\s(?<date>.+).".toRegex()

data class Result(
    val file: File,
    val name: String,
    val date: Date
)

fun main(args: Array<String>) {
    val command = "grep -r \"Created by\""
    val results =
        SimpleCommandExecutor.executeCommands(
            arrayOf("cd ${System.getProperty("user.dir")} && $command"),
            isLivePrint = false,
            isSuppressError = false,
            isReturnAll = false
        )

    if (results.isNotEmpty()) {
        val resultList = mutableListOf<Result>()
        for (result in results) {

            val matcher = REGEX.find(result)
            if (matcher != null) {
                val file = File(matcher.groups["file"]!!.value)
                val name = matcher.groups["name"]!!.value
                val date = parseDate(matcher.groups["date"]!!.value)
                resultList.add(Result(file, name, date))
            }
        }

        if (resultList.isNotEmpty()) {

            // Analyzing result
            val fileMatrix = mutableMapOf<String, Int>()
            val dateMatrix = mutableMapOf<Result, Long>()
            val curDate = Date()
            for (r in resultList) {
                fileMatrix[r.name] = fileMatrix[r.name]?.plus(1) ?: 1
                val diff = curDate.time - r.date.time
                dateMatrix[r] = diff
            }

            // sort
            val sortedFileMatrix = fileMatrix.toList().sortedByDescending { (_, value) -> value }.toMap()

            println("Name | File Count")
            println("-----------------")

            for (name in sortedFileMatrix) {
                println("${name.key} - ${name.value}")
            }

            println("--------------------")
            val lastEdited = dateMatrix.toList().minBy { (_, value) -> value }!!.first
            println("Last edited by ${lastEdited.name} -  ${DATE_FORMAT.format(lastEdited.date)}")
        } else {
            println("Sorry, couldn't find who-wrote")
        }
    } else {
        println("Sorry, file signatures not found.")
    }


}

val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy")
fun parseDate(_date: String): Date {
    val dateString = _date.replace("-", "/")
    return DATE_FORMAT.parse(dateString)
}
