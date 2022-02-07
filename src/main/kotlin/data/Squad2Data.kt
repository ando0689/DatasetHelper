package data

import androidx.compose.ui.text.toLowerCase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*


@Serializable
data class Squad2Data(
    val `data`: List<Data>,
    val version: String = "v2.0",
    @Transient val path: String = ""
){
    val name: String
        get() = File(path).name

    companion object {
        fun open(file: File): Squad2Data? {
            return try {
                Json.decodeFromString(serializer(), file.readText()).copy(path = file.absolutePath)
            } catch (t: Throwable){
                t.printStackTrace()
                null
            }
        }

        fun new(name: String): Squad2Data {
            val dir = "${System.getProperty("user.home")}/Desktop/datasets/squad2"
            File(dir).also { it.mkdirs() }
            val file = File(dir, "$name.json")
            file.createNewFile()
            return Squad2Data(path = file.absolutePath, data = emptyList())
        }
    }

    fun toQuestionsTsv(): Pair<String, String> {
        val questions = data
            .flatMap { it.paragraphs }
            .flatMap { p ->
                val label = p.context.split(" --").first().replace("-- ", "").lowercase()
                p.qas.filter { !it.isImpossible }.map { qa -> "${qa.question},$label" }
        }

        val all = questions.shuffled()
        val splitIndex = (all.size * 0.9).toInt()
        val train = all.subList(0, splitIndex).mapIndexed { i, s -> "$i,$s" }
        val test = all.subList(splitIndex, all.size).mapIndexed { i, s -> "$i,$s" }

        fun List<String>.convertToString() = ",sentence,label\n" + joinToString("\n")

        return train.convertToString() to test.convertToString()
    }

    fun save(){
        val jsonString = Json.encodeToString(serializer(), this)
        val jsonFile = File(path)
        val trainCsv = File(path.replace(".json", "_train_.csv")).also { it.createNewFile() }
        val testCsv = File(path.replace(".json", "_test_.csv")).also { it.createNewFile() }
        jsonFile.writeText(jsonString)
        trainCsv.writeText(toQuestionsTsv().first)
        testCsv.writeText(toQuestionsTsv().second)
    }
}

@Serializable
data class Data(
    val paragraphs: List<Paragraph>,
    val title: String
)

@Serializable
data class Paragraph(
    val context: String,
    val qas: List<Qa>
)

@Serializable
data class Qa(
    val answers: List<Answer>,
    val id: String,
    @SerialName("is_impossible")
    val isImpossible: Boolean,
    val question: String
)

@Serializable
data class Answer(
    @SerialName("answer_start")
    val answerStart: Int,
    val text: String
)
