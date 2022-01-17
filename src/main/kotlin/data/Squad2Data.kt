package data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Squad2Data(
    val `data`: List<Data>,
    val version: String = "v2.0"
){
    companion object {
        fun open(file: File): Squad2Data? {
            return runCatching {
                Json.decodeFromString(serializer(), file.readText())
            }.getOrNull()
        }

        fun empty(): Squad2Data{
            return Squad2Data(emptyList())
        }
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
    val isImpossible: Boolean,
    val question: String
)

@Serializable
data class Answer(
    val answerStart: Int,
    val text: String
)