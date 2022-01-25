package data

import androidx.compose.runtime.mutableStateListOf
import squad2.CommonRowState
import squad2.CommonRowStateHolder
import java.util.*

class Squad2DataState(squad2Data: Squad2Data? = null): CommonRowStateHolder {
    val path = CommonRowState(value = squad2Data?.path ?: "")
    val data = mutableStateListOf(*squad2Data?.data?.map { DataState(this, it) }?.toTypedArray() ?: emptyArray())

    fun toSquad2Data() = Squad2Data(
        path = path.text,
        data = data.map { it.toData() }
    )

    fun save(){
        toSquad2Data().save()
    }

    override val commonRowState: CommonRowState
        get() = path
}

class DataState(val parent: Squad2DataState, data: Data? = null): CommonRowStateHolder {
    val title = CommonRowState(value = data?.title ?: "")
    val paragraphs = mutableStateListOf(*data?.paragraphs?.map { ParagraphState(this, it) }?.toTypedArray() ?: emptyArray())

    fun toData() = Data(
        title = title.text,
        paragraphs = paragraphs.map { it.toParagraph() }
    )

    fun save(){
        parent.save()
    }

    override val commonRowState: CommonRowState
        get() = title
}

class ParagraphState(val parent: DataState, paragraph: Paragraph? = null): CommonRowStateHolder {
    val context = CommonRowState(value = paragraph?.context ?: "")
    val qas = mutableStateListOf(*paragraph?.qas?.map { QaState(this, it) }?.toTypedArray() ?: emptyArray())

    fun toParagraph() = Paragraph(
        context = context.text,
        qas = qas.map { it.toQa() }
    )

    fun save(){
        parent.save()
    }

    override val commonRowState: CommonRowState
        get() = context
}

class QaState(val parent: ParagraphState, qa: Qa? = null): CommonRowStateHolder {
    val answers = mutableStateListOf(*qa?.answers?.map { AnswerState(this, it) }?.toTypedArray() ?: emptyArray())
    var question = CommonRowState(value = qa?.question ?: "", note = if(answers.isEmpty()) "No Answer" else null)
    val id = if(qa?.id.isNullOrBlank()) UUID.randomUUID().toString() else qa?.id!!
    val context: String
        get() = parent.context.text

    fun toQa() = Qa(
        answers = answers.map { it.toAnswer() },
        id = id,
        isImpossible = answers.isEmpty(),
        question = question.text
    )

    fun save(){
        parent.save()
    }

    override val commonRowState: CommonRowState
        get() = question
}

class AnswerState(val parent: QaState, answer: Answer? = null): CommonRowStateHolder{
    val text = CommonRowState(value = answer?.text ?: "")
    val context: String
        get() = parent.context

    override fun isValid() = text.text.isNotBlank() && context.contains(text.text)

    override fun errorText() = "Answer is not part of the context."

    fun toAnswer() = Answer(
        answerStart = context.indexOf(text.text),
        text = text.text
    )

    fun save(){
        parent.save()
    }

    override val commonRowState: CommonRowState
        get() = text
}
