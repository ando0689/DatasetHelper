package data

import androidx.compose.runtime.mutableStateListOf
import squad2.CommonRowState
import squad2.CommonRowStateHolder
import java.util.*

class PlainParagraphState(parent: DataState, paragraph: Paragraph? = null): ParagraphState(parent, paragraph) {
    val questions = mutableStateListOf(*paragraph?.qas?.map { PlainQaState(this, it) }?.toTypedArray() ?: emptyArray())

    override val qas: List<Qa>
        get() = questions.map { it.toQa() }
}


class PlainQaState(val parent: PlainParagraphState, qa: Qa? = null): CommonRowStateHolder {
    val answers = mutableStateListOf(*qa?.answers?.map { PlainAnswerState(this, it) }?.toTypedArray() ?: emptyArray())
    var question = CommonRowState(value = qa?.question ?: "").also { setNoteIfNoAnswers(it) }
    val id = if(qa?.id.isNullOrBlank()) UUID.randomUUID().toString() else qa?.id!!
    val context: String
        get() = parent.context.text

    fun toQa() = Qa(
        answers = answers.map { it.toAnswer() },
        id = id,
        isImpossible = answers.isEmpty(),
        question = question.text
    )

    fun setNoteIfNoAnswers() = setNoteIfNoAnswers(question)

    private fun setNoteIfNoAnswers(q: CommonRowState){
        q.noteText = if(answers.isEmpty()) "No Answer" else null
    }

    fun save(){
        parent.save()
    }

    override val commonRowState: CommonRowState
        get() = question
}

class PlainAnswerState(private val parent: PlainQaState, answer: Answer? = null): AnswerState {
    override val text = CommonRowState(value = answer?.text ?: "")
    override val context: String
        get() = parent.context

    override fun isValid() = text.text.isNotBlank() && context.contains(text.text)
}
