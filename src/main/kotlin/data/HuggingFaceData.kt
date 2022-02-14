package data

data class HuggingFaceData(
    val context: String,
    val question: String,
    val answerText: String,
    val answerStart: Int
) {
    companion object {
        fun fromSquad(data: Squad2Data): List<HuggingFaceData> {
            return data.data
                .flatMap { it.paragraphs }
                .flatMap { p -> p.qas.map { p.context to it } }
                .filter { !it.second.isImpossible && it.second.answers.isNotEmpty() }
                .map {
                    HuggingFaceData(
                        context = it.first,
                        question = it.second.question,
                        answerText = it.second.answers.first().text,
                        answerStart = it.second.answers.first().answerStart
                    )
                }
        }
    }

    fun toTsv(): String {
        return "{\"context\":\"$context\", \"question\":\"$question\", \"answers_text\":[\"$answerText\"], \"answers_answer_start\":[$answerStart]}"
    }
}