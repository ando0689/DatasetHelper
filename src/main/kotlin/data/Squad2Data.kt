package data

import androidx.compose.ui.text.toLowerCase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import kotlin.math.roundToInt

@Serializable
data class Squad2Data(
    val `data`: List<Data>,
    val version: String = "v2.0",
    @Transient val path: String = ""
){
    val name: String
        get() = File(path).name

    companion object {
        fun open(file: File): Squad2Data? {
            return try {
                Json.decodeFromString(serializer(), file.readText()).copy(path = file.absolutePath)
            } catch (t: Throwable){
                t.printStackTrace()
                null
            }
        }

        fun new(name: String): Squad2Data {
            val dir = "${System.getProperty("user.home")}/Desktop/datasets/squad2"
            File(dir).also { it.mkdirs() }
            val file = File(dir, "$name.json")
            file.createNewFile()
            return Squad2Data(path = file.absolutePath, data = emptyList())
        }
    }

    private fun splitTrainTestData(): Pair<Squad2Data, Squad2Data> {

        fun Data.split(): Pair<Data, Data> {
            val allParagraphs = paragraphs.onEach {
                it.qas.shuffled()
            }

            val splitParagraphs = allParagraphs.map { p ->

                val splits: Map<Answer, Pair<List<Qa>, List<Qa>>> = p.qas.groupBy { it.answers.first() }.mapValues {
                    val splitIndex = (it.value.size * 0.8).roundToInt()
                    it.value.subList(0, splitIndex) to it.value.subList(splitIndex, it.value.size)
                }

                val trainQas = splits.flatMap { it.value.first }
                val testQas = splits.flatMap { it.value.second }
                p.copy(qas = trainQas) to p.copy(qas = testQas)
            }

//            val splitParagraphs: List<Pair<Paragraph, Paragraph>> = allParagraphs.map { p ->
//                val splitIndex = (p.qas.size * 0.8).toInt()
//                val trainQas = p.qas.subList(0, splitIndex)
//                val testQas = p.qas.subList(splitIndex, p.qas.size)
//
//                p.copy(qas = trainQas) to p.copy(qas = testQas)
//            }

            val trainParagraphs = splitParagraphs.map { it.first }
            val testParagraphs = splitParagraphs.map { it.second }

            return copy(paragraphs = trainParagraphs) to copy(paragraphs = testParagraphs)
        }

        val splitData: List<Pair<Data, Data>> = data.map { it.split() }

        val trainData = splitData.map { it.first }
        val testData = splitData.map { it.second }

        return copy(data = trainData) to copy(data = testData)

//        val splitParagraphs = allParagraphs.map { p ->
//
//            val splits: Map<Answer, Pair<List<Qa>, List<Qa>>> = p.qas.groupBy { it.answers.first() }.mapValues {
//                val splitIndex = (it.value.size * 0.85).toInt()
//                it.value.subList(0, splitIndex) to it.value.subList(splitIndex, it.value.size)
//            }
//
//            val trainQas = splits.flatMap { it.value.first }
//            val testQas = splits.flatMap { it.value.second }
//            Paragraph(p.context, trainQas) to Paragraph(p.context, testQas)
//        }
    }


    fun save(){
        saveMainJson()
        saveQuestionsJsonl()
        saveQuestionAnswersSplit()
    }

    fun saveQuestionAnswersSplit(){
        val trainTestData = splitTrainTestData()
        val trainJsonString = Json.encodeToString(serializer(), trainTestData.first)
        val testJsonString = Json.encodeToString(serializer(), trainTestData.second)

        val trainJsonFile = File(path.replace(".json", "qa_train.json")).also { it.createNewFile() }
        val testJsonFile = File(path.replace(".json", "qa_test.json")).also { it.createNewFile() }

        trainJsonFile.writeText(trainJsonString)
        testJsonFile.writeText(testJsonString)
    }

    fun saveQuestionsJsonl(){
        val questions = data.flatMap { d -> d.paragraphs.flatMap { p -> p.qas.filter { !it.isImpossible }.map { q -> q.question to d.title.lowercase() } } }

        val all = questions.shuffled()
        val splitIndex = (all.size * 0.8).toInt()
        val train = all.subList(0, splitIndex)
        val test = all.subList(splitIndex, all.size)

        fun List<Pair<String, String>>.convertToJsonl() = joinToString("\n") {
            "{\"sentence1\": \"${it.first}\", \"label\": \"${it.second}\"}"
        }

        val trainFile = File(path.replace(".json", "class_hf_train.json")).also { it.createNewFile() }
        val testFile = File(path.replace(".json", "class_hf_test.json")).also { it.createNewFile() }

        trainFile.writeText(train.convertToJsonl())
        testFile.writeText(test.convertToJsonl())
    }

    fun saveQuestionsCsv(){
        val questions = data.flatMap { d -> d.paragraphs.flatMap { p -> p.qas.map { q -> "${q.question},${d.title}" } } }

        val all = questions.shuffled()
        val splitIndex = (all.size * 0.8).toInt()
        val train = all.subList(0, splitIndex).mapIndexed { i, s -> "$i,$s" }
        val test = all.subList(splitIndex, all.size).mapIndexed { i, s -> "$i,$s" }

        fun List<String>.convertToString() = ",sentence,label\n" + joinToString("\n")

        val trainCsvFile = File(path.replace(".json", "_train.csv")).also { it.createNewFile() }
        val testCsvFile = File(path.replace(".json", "_test.csv")).also { it.createNewFile() }

        trainCsvFile.writeText(train.convertToString())
        testCsvFile.writeText(test.convertToString())
    }

    fun saveHuggingFacesCsv(){
        val cscFile = File(path.replace(".json", "_hf.jsonl")).also { it.createNewFile() }
        val text = HuggingFaceData.fromSquad(this).joinToString("\n") { it.toTsv() }
        cscFile.writeText(text)
    }

    private fun saveMainJson(){
        val jsonString = Json.encodeToString(serializer(), this)
        val jsonFile = File(path)
        jsonFile.writeText(jsonString)
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
    val plausible_answers: List<Answer> = emptyList(),
    val id: String,
    @SerialName("is_impossible")
    val isImpossible: Boolean,
    val question: String
)

@Serializable
data class Answer(
    @SerialName("answer_start")
    val answerStart: Int,
    val text: String
)
