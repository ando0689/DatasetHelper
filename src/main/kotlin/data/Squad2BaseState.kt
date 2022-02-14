package data

import androidx.compose.runtime.mutableStateListOf
import squad2.CommonRowState
import squad2.CommonRowStateHolder
import java.io.File

class Squad2DataState(squad2Data: Squad2Data? = null, val groupQuestions: Boolean = false): CommonRowStateHolder {
    val path = CommonRowState(value = squad2Data?.path ?: "")
    val data = mutableStateListOf(*squad2Data?.data?.map { DataState(this, it) }?.toTypedArray() ?: emptyArray())

    val fileName: String
        get() = File(path.text).name


    val datasetsSize: Int
        get() = data.size

    val paragraphsSize: Int
        get() = data.sumOf { it.paragraphsSize }

    val questionsSize: Int
        get() = data.sumOf { it.questionsSize }

    val answerableQuestionsSize: Int
        get() = data.sumOf { it.answerableQuestionsSize }

    fun toSquad2Data() = Squad2Data(
        path = path.text,
        data = data.map { it.toData() }
    )

    fun getSummery(): String {
        return "Datasets: $datasetsSize, Paragraphs: $paragraphsSize, Questions: $questionsSize, Answerable: $answerableQuestionsSize"
    }

    fun save(){
        toSquad2Data().save()
    }

    override val commonRowState: CommonRowState
        get() = path
}

class DataState(val parent: Squad2DataState, data: Data? = null): CommonRowStateHolder {
    val title = CommonRowState(value = data?.title ?: "")
    val paragraphs = mutableStateListOf(*data?.paragraphs?.map {
        if(parent.groupQuestions) GroupParagraphState(this, it) else PlainParagraphState(this, it)
    }?.toTypedArray() ?: emptyArray())

    val paragraphsSize: Int
        get() = paragraphs.size

    val questionsSize: Int
        get() = paragraphs.sumOf { it.questionsSize }

    val answerableQuestionsSize: Int
        get() = paragraphs.sumOf { it.answerableQuestionsSize }

    fun toData() = Data(
        title = title.text,
        paragraphs = paragraphs.map { it.toParagraph() }
    )

    fun getSummery(): String {
        return "Paragraphs: $paragraphsSize, Questions: $questionsSize, Answerable: $answerableQuestionsSize"
    }

    fun save(){
        parent.save()
    }

    override val commonRowState: CommonRowState
        get() = title
}

abstract class ParagraphState(val parent: DataState, paragraph: Paragraph?): CommonRowStateHolder {
    abstract val qas: List<Qa>

    companion object {
        fun new(parent: DataState, paragraph: Paragraph? = null): ParagraphState {
            return if(parent.parent.groupQuestions) GroupParagraphState(parent, paragraph) else PlainParagraphState(parent, paragraph)
        }
    }

    val questionsSize: Int
        get() = qas.size

    val answerableQuestionsSize: Int
        get() = qas.filter { it.answers.isNotEmpty() }.size

    fun toParagraph() = Paragraph(
        context = context.text,
        qas = qas
    )

    val context = CommonRowState(value = paragraph?.context ?: "")

    fun save(){
        parent.save()
    }

    override fun errorText() = "Some answers do not match this context"

    fun getSummery(): String {
        return "Questions: $questionsSize, Answerable: $answerableQuestionsSize"
    }

    override val commonRowState: CommonRowState
        get() = context
}


interface AnswerState: CommonRowStateHolder {
    val text: CommonRowState
    val context: String

    override fun errorText() = "Answer is not part of the context."

    override val commonRowState: CommonRowState
        get() = text

    fun toAnswer() = Answer(
        answerStart = context.indexOf(text.text),
        text = text.text
    )
}