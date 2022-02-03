package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.File


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

    fun toQuestionsTsv(): String {
        val questions = data
            .flatMap { it.paragraphs }
            .flatMapIndexed { i, p ->
                p.qas.filter { !it.isImpossible }.map { "${it.question}\t$i" }
        }

        return "sentence\tlabel\n" + questions.shuffled().joinToString("\n")
    }

    fun save(){
        val jsonString = Json.encodeToString(serializer(), this)
        val jsonFile = File(path)
        val tsvFile = File(path.replace(".json", ".tsv")).also { it.createNewFile() }
        jsonFile.writeText(jsonString)
        tsvFile.writeText(toQuestionsTsv())
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
