package data

import androidx.compose.runtime.mutableStateListOf
import squad2.CommonRowState
import squad2.CommonRowStateHolder
import java.util.*

class Squad2DataState(squad2Data: Squad2Data? = null): CommonRowStateHolder {
    val path = CommonRowState(value = squad2Data?.path ?: "")
    val data = mutableStateListOf(*squad2Data?.data?.map { DataState(this, it) }?.toTypedArray() ?: emptyArray())

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
    val paragraphs = mutableStateListOf(*data?.paragraphs?.map { ParagraphState(this, it) }?.toTypedArray() ?: emptyArray())

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

class ParagraphState(val parent: DataState, paragraph: Paragraph? = null): CommonRowStateHolder {
    val context = CommonRowState(value = paragraph?.context ?: "")
    val qas = mutableStateListOf(*paragraph?.qas?.map { QaState(this, it) }?.toTypedArray() ?: emptyArray())

    val questionsSize: Int
        get() = qas.size

    val answerableQuestionsSize: Int
        get() = qas.filter { it.answers.isNotEmpty() }.size

    fun toParagraph() = Paragraph(
        context = context.text,
        qas = qas.map { it.toQa() }
    )

    fun save(){
        parent.save()
    }

    fun getSummery(): String {
        return "Questions: $questionsSize, Answerable: $answerableQuestionsSize"
    }

    override val commonRowState: CommonRowState
        get() = context
}

class QaState(val parent: ParagraphState, qa: Qa? = null): CommonRowStateHolder {
    val answers = mutableStateListOf(*qa?.answers?.map { AnswerState(this, it) }?.toTypedArray() ?: emptyArray())
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

class AnswerState(private val parent: QaState, answer: Answer? = null): CommonRowStateHolder{
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
