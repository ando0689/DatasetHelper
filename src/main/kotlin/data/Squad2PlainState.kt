package data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import squad2.CommonRowState
import squad2.CommonRowStateHolder
import java.util.*

class PlainParagraphState(parent: DataState, paragraph: Paragraph? = null): ParagraphState(parent, paragraph) {
    val questions = mutableStateListOf(*paragraph?.qas?.map { PlainQaState(this, it) }?.toTypedArray() ?: emptyArray())

    override val qas: List<Qa>
        get() = questions.map { it.toQa() }

    override fun isValid(): Boolean {
        return questions.all { it.isValid() }
    }
}


class PlainQaState(val parent: PlainParagraphState, qa: Qa? = null): CommonRowStateHolder {
    val answers = mutableStateListOf(*qa?.answers?.map { PlainAnswerState(this, it) }?.toTypedArray() ?: emptyArray())
    var isImpossible by mutableStateOf(qa?.isImpossible ?: false)
    var question = CommonRowState(value = qa?.question ?: "").also { setNoteIfNoAnswers(it) }
    val id = if(qa?.id.isNullOrBlank()) UUID.randomUUID().toString() else qa?.id!!
    val context: String
        get() = parent.context.text

    fun toQa():Qa {
       val qa = Qa(
            answers = if(isImpossible) emptyList() else answers.map { it.toAnswer() },
            plausible_answers = if(isImpossible) answers.map { it.toAnswer() } else emptyList(),
            id = id,
            isImpossible = isImpossible,
            question = question.text
        )

        return qa
    }

    fun setNoteIfNoAnswers() = setNoteIfNoAnswers(question)

    private fun setNoteIfNoAnswers(q: CommonRowState){
        q.noteText = if(answers.isEmpty()) "No Answer" else null
    }

    fun save(){
        parent.save()
    }

    override fun isValid(): Boolean {
        return answers.all { it.isValid() }
    }

    override fun errorText() = "Some answer of this question does not match the context"

    override val commonRowState: CommonRowState
        get() = question
}

class PlainAnswerState(private val parent: PlainQaState, answer: Answer? = null): AnswerState {
    override val text = CommonRowState(value = answer?.text ?: "")
    override val context: String
        get() = parent.context

    override fun isValid() = text.text.isNotBlank() && context.contains(text.text)
}
