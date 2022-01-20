package data

import androidx.compose.runtime.mutableStateListOf
import squad2.CommonRowState
import squad2.CommonRowStateHolder
import java.util.*

class Squad2DataState(squad2Data: Squad2Data? = null): CommonRowStateHolder {
    val name = CommonRowState(value = squad2Data?.name ?: "")
    val data = mutableStateListOf(*squad2Data?.data?.map { DataState(this, it) }?.toTypedArray() ?: emptyArray())

    fun toSquad2Data() = Squad2Data(
        name = name.text,
        data = data.map { it.toData() }
    )

    override val commonRowState: CommonRowState
        get() = name
}

class DataState(val parent: Squad2DataState, data: Data? = null): CommonRowStateHolder {
    val title = CommonRowState(value = data?.title ?: "")
    val paragraphs = mutableStateListOf(*data?.paragraphs?.map { ParagraphState(this, it) }?.toTypedArray() ?: emptyArray())

    fun toData() = Data(
        title = title.text,
        paragraphs = paragraphs.map { it.toParagraph() }
    )

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

    override val commonRowState: CommonRowState
        get() = context
}

class QaState(val parent: ParagraphState, qa: Qa? = null): CommonRowStateHolder {
    var question = CommonRowState(value = qa?.question ?: "")
    val answers = mutableStateListOf(*qa?.answers?.map { AnswerState(this, it) }?.toTypedArray() ?: emptyArray())
    val context: String
        get() = parent.context.text


    fun toQa() = Qa(
        answers = answers.map { it.toAnswer() },
        id = UUID.randomUUID().toString(),
        isImpossible = answers.isEmpty(),
        question = question.text
    )

    override val commonRowState: CommonRowState
        get() = question
}

class AnswerState(val parent: QaState, answer: Answer? = null): CommonRowStateHolder{
    val text = CommonRowState(value = answer?.text ?: "")
    val context: String
        get() = parent.context

    fun isValid() = text.text.isNotBlank() && context.contains(text.text)

    fun toAnswer() = Answer(
        answerStart = context.indexOf(text.text),
        text = text.text
    )

    override val commonRowState: CommonRowState
        get() = text
}
