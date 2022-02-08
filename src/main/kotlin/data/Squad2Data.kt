package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.IllegalStateException


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
        val allParagraphs = data.first().paragraphs.onEach {
            it.qas.shuffled()
        }

        val splitParagraphs = allParagraphs.map { p ->

            val splits: Map<Answer, Pair<List<Qa>, List<Qa>>> = p.qas.groupBy { it.answers.first() }.mapValues {
                val splitIndex = (it.value.size * 0.85).toInt()
                it.value.subList(0, splitIndex) to it.value.subList(splitIndex, it.value.size)
            }

            val trainQas = splits.flatMap { it.value.first }
            val testQas = splits.flatMap { it.value.second }
            Paragraph(p.context, trainQas) to Paragraph(p.context, testQas)
        }

        val trainParagraphs = splitParagraphs.map { it.first }
        val testParagraphs = splitParagraphs.map { it.second }

        val trainData = Squad2Data(
            version = version,
            data = listOf(
                Data(trainParagraphs, data.first().title)
            )
        )

        val testData = Squad2Data(
            version = version,
            data = listOf(
                Data(testParagraphs, data.first().title)
            )
        )

        return trainData to testData
    }

    private fun toQuestionsCsv(): Pair<String, String> {
        val questions = data
            .flatMap { it.paragraphs }
            .flatMap { p ->
                val label = p.context.split(" --").first().replace("-- ", "").lowercase()
                p.qas.filter { !it.isImpossible }.map { qa -> "${qa.question},$label" }
        }

        val all = questions.shuffled()
        val splitIndex = (all.size * 0.8).toInt()
        val train = all.subList(0, splitIndex).mapIndexed { i, s -> "$i,$s" }
        val test = all.subList(splitIndex, all.size).mapIndexed { i, s -> "$i,$s" }

        fun List<String>.convertToString() = ",sentence,label\n" + joinToString("\n")

        return train.convertToString() to test.convertToString()
    }

    fun save(){
//        if(data.size > 1) throw IllegalStateException("More then one dataset is not supported now")
        val jsonString = Json.encodeToString(serializer(), this)

//        val trainTestData = splitTrainTestData()
//        val trainJsonString = Json.encodeToString(serializer(), trainTestData.first)
//        val testJsonString = Json.encodeToString(serializer(), trainTestData.second)

        val jsonFile = File(path)

//        val trainJsonFile = File(path.replace(".json", "_train.json")).also { it.createNewFile() }
//        val testJsonFile = File(path.replace(".json", "_test.json")).also { it.createNewFile() }

//        val trainCsvFile = File(path.replace(".json", "_train.csv")).also { it.createNewFile() }
//        val testCsvFile = File(path.replace(".json", "_test.csv")).also { it.createNewFile() }

        jsonFile.writeText(jsonString)
//
//        trainJsonFile.writeText(trainJsonString)
//        testJsonFile.writeText(testJsonString)

//        trainCsvFile.writeText(toQuestionsCsv().first)
//        testCsvFile.writeText(toQuestionsCsv().second)
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
    val plausible_answers: List<Answer>,
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
