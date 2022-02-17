package data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
data class HFAnswer(
    val text: List<String>,
    val answer_start: List<Int>
)

@Serializable
data class HuggingFaceDataItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val context: String,
    val question: String,
    val answers: HFAnswer
)

@Serializable
data class HuggingFaceData(
    val data: List<HuggingFaceDataItem>
) {
    companion object {
        fun fromSquad(data: Squad2Data): HuggingFaceData {
            return HuggingFaceData(data.data.flatMap { it.toHFData() })
        }

        private fun Data.toHFData(): List<HuggingFaceDataItem> {
            return paragraphs.flatMap { p -> p.qas.map { p.context to it } }
                .filter { !it.second.isImpossible && it.second.answers.isNotEmpty() }
                .map {
                    HuggingFaceDataItem(
                        title = title,
                        context = it.first,
                        question = it.second.question,
                        answers = HFAnswer(
                            text = it.second.answers.map { a -> a.text },
                            answer_start = it.second.answers.map { a -> a.answerStart },
                        )
                    )
                }
        }
    }

    fun toJsonString(): String {
        return Json.encodeToString(serializer(), this)
    }
}