package data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import squad2.CommonRowState
import squad2.CommonRowStateHolder
import java.util.*

class GroupParagraphState(parent: DataState, paragraph: Paragraph? = null): ParagraphState(parent, paragraph) {
    private val groupsMap: Map<Answer?, List<Qa>>? = paragraph?.qas?.groupBy { it.answers.firstOrNull() }
    val questionGroups = mutableStateListOf(*groupsMap?.map { GroupQaState(this, it.value, it.key) }?.toTypedArray() ?: emptyArray())

    override fun isValid(): Boolean {
        return questionGroups.all { it.isValid() }
    }

    override val qas: List<Qa>
        get() = questionGroups.flatMap { it.toQas() }
}

class GroupQaState(val parent: GroupParagraphState, qas: List<Qa>? = null, answer: Answer? = null): CommonRowStateHolder {
    var note by mutableStateOf<String?>(null)
    val answer = GroupedAnswerState(this, answer)
    val questions = mutableStateListOf(*qas?.map { GroupQuestionItemState(this, it) }?.toTypedArray() ?: arrayOf(GroupQuestionItemState(this)))
    var isImpossible: Boolean
        get() = questions.firstOrNull()?.isImpossible ?: false
        set(value) {
            questions.forEach { it.isImpossible = value }
        }


    val context: String
        get() = parent.context.text

    init {
        setNote()
    }

    fun toQas() = questions.map {
        it.toQa(answer)
    }

    fun setNote(){
        note = if(answer.text.text.isBlank()) " ${questions.size}  with No Answer" else " ${questions.size} "
    }

    fun save(){
        parent.save()
    }

    override fun isValid(): Boolean {
        return answer.isValid()
    }

    override fun errorText() = "Answer of this group does not match the context"

    override val commonRowState: CommonRowState
        get() = questions.first().commonRowState.also { it.noteText = note }
}

class GroupQuestionItemState(val parent: GroupQaState, qa: Qa? = null): CommonRowStateHolder {
    var question = CommonRowState(value = qa?.question ?: "")
    val id = if(qa?.id.isNullOrBlank()) UUID.randomUUID().toString() else qa?.id!!
    var isImpossible by mutableStateOf(qa?.isImpossible ?: false)

    fun answers(answer: GroupedAnswerState) = if(answer.text.text.isNotBlank()) listOf(answer.toAnswer()) else emptyList()

    fun toQa(answer: GroupedAnswerState): Qa {
        val qa = Qa(
            answers = if(isImpossible) emptyList() else answers(answer),
            plausible_answers = if(isImpossible) answers(answer) else emptyList(),
            id = id,
            isImpossible = isImpossible,
            question = question.text
        )
        return qa
    }

    fun save(){
        parent.save()
    }

    override val commonRowState: CommonRowState
        get() = question
}

class GroupedAnswerState(private val parent: GroupQaState, answer: Answer? = null): AnswerState {
    override val text = CommonRowState(value = answer?.text ?: "")
    override val context: String
        get() = parent.context

    override fun isValid() = text.text.isBlank() || context.contains(text.text)
}