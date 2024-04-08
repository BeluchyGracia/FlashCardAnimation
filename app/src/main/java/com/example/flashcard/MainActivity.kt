package com.example.flashcard

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit
import kotlin.math.max

class MainActivity : AppCompatActivity() {
    private val ADD_CARD_REQUEST = 1
    private val SAVE_CARD_REQUEST = 2
    private lateinit var flashcardDatabase: FlashcardDatabase
    private var allFlashcards = mutableListOf<Flashcard>()
    private var currentCardDisplayedIndex= 0
    private lateinit var leftOutAnim: Animation
    private lateinit var  rightInAnim: Animation
    var countDownTimer: CountDownTimer? = null
    private lateinit var viewKonfetti : KonfettiView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        leftOutAnim = AnimationUtils.loadAnimation(this, R.anim.left_out)
        rightInAnim = AnimationUtils.loadAnimation(this, R.anim.right_in)
        viewKonfetti = findViewById<KonfettiView>(R.id.konfettiView)


        var flashcardQuestion = findViewById<TextView>(R.id.flashcard_question)
        var flashcardAnswer = findViewById<TextView>(R.id.flashcard_answer)
        var flashcardAnswer2 = findViewById<TextView>(R.id.flashcard_answer2)
        var flashcardAnswer3 = findViewById<TextView>(R.id.flashcard_answer3)

        var bontonPlus = findViewById<View>(R.id.pbtn)
        var boutonEdit = findViewById<View>(R.id.editBtn)
        var botnNext= findViewById<ImageView>(R.id.rightbtn)
        var deletebtn=findViewById<ImageView>(R.id.poubtn)


        flashcardDatabase = FlashcardDatabase(this)
        flashcardDatabase.initFirstCard()
        allFlashcards = flashcardDatabase.getAllCards().toMutableList()



        fun setFlashcardAnswerClickListener(flashcardView: View, flashcardIndex: Int) {
            flashcardView.setOnClickListener {
                val answerText = (flashcardView as TextView).text.toString()
                if (answerText.isNotEmpty()) {
                    val correctAnswer = allFlashcards[currentCardDisplayedIndex].answer
                    val isCorrect = answerText == correctAnswer
                    val colorRes = if (isCorrect) R.color.green else R.color.red

                    flashcardView.setBackgroundColor(getResources().getColor(colorRes))
                    if (isCorrect) {
                        viewKonfetti.start(party)
                        Handler().postDelayed({
                            flashcardView.setBackgroundColor(getResources().getColor(R.color.Rose))
                            botnNext.performClick()
                        }, 500)
                    }

                    flashcardAnswer.isEnabled = false
                    flashcardAnswer2.isEnabled = false
                    flashcardAnswer3.isEnabled = false

                    Handler().postDelayed({
                        flashcardView.setBackgroundColor(getResources().getColor(R.color.Rose))
                        flashcardAnswer.isEnabled = true
                        flashcardAnswer2.isEnabled = true
                        flashcardAnswer3.isEnabled = true
                    }, 1000)
                }
            }
        }

        setFlashcardAnswerClickListener(flashcardAnswer, 0)
        setFlashcardAnswerClickListener(flashcardAnswer2, 1)
        setFlashcardAnswerClickListener(flashcardAnswer3, 2)




        if (allFlashcards.size > 0) {
            flashcardQuestion.text = allFlashcards[0].question
            flashcardAnswer.text = allFlashcards[0].answer
            flashcardAnswer2.text = allFlashcards[0].wrongAnswer1
            flashcardAnswer3.text = allFlashcards[0].wrongAnswer2

        }
        leftOutAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                findViewById<View>(R.id.flashcard_question).startAnimation(rightInAnim)
                findViewById<View>(R.id.flashcard_answer).startAnimation(rightInAnim)
                findViewById<View>(R.id.flashcard_answer2).startAnimation(rightInAnim)
                findViewById<View>(R.id.flashcard_answer3).startAnimation(rightInAnim)

            }

            override fun onAnimationRepeat(animation: Animation?) {
            }



        })
        countDownTimer = object : CountDownTimer(16000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                findViewById<TextView>(R.id.time).text = " reload time: " + millisUntilFinished / 1000
            }

            override fun onFinish() {
                botnNext.performClick()
            }
        }
        startTimer()







        bontonPlus.setOnClickListener {
            val i = Intent(this, AddCardActivity::class.java)
            resultLauncher.launch(i)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }

        boutonEdit.setOnClickListener {
            val (question, answer, wrongAnswer1, wrongAnswer2, uuid) = allFlashcards[currentCardDisplayedIndex]

            val intent = Intent(this, AddCardActivity::class.java)

            intent.putExtra("question", question)
            intent.putExtra("answer1", answer)
            intent.putExtra("answer2", wrongAnswer1)
            intent.putExtra("answer3", wrongAnswer2)
            intent.putExtra("uuid", uuid)
            startActivityForResult(intent, ADD_CARD_REQUEST)
        }

        botnNext.setOnClickListener {
            if (allFlashcards.isEmpty()) {
                return@setOnClickListener
            }

            currentCardDisplayedIndex = getRandomNumber(0,allFlashcards.size-1)

            if (currentCardDisplayedIndex >= allFlashcards.size) {
                currentCardDisplayedIndex = 0


            }

            val (question, answer,wrongAnswer1,wrongAnswer2) = allFlashcards[currentCardDisplayedIndex]

            flashcardQuestion.text = question
            flashcardAnswer.text = answer
            flashcardAnswer2.text = wrongAnswer1
            flashcardAnswer3.text = wrongAnswer2


            flashcardQuestion.startAnimation(leftOutAnim)
            flashcardAnswer.startAnimation(leftOutAnim)
            flashcardAnswer2.startAnimation(leftOutAnim)
            flashcardAnswer3.startAnimation(leftOutAnim)

            startTimer()
        }

        deletebtn.setOnClickListener {
            val currentQuestion = flashcardQuestion.text.toString()
            flashcardDatabase.deleteCard(currentQuestion)

            allFlashcards = flashcardDatabase.getAllCards().toMutableList()


            if (allFlashcards.isNotEmpty()) {

                currentCardDisplayedIndex = max(0, currentCardDisplayedIndex - 1)
                val (question, answer,wrongAnswer1,wrongAnswer2) = allFlashcards[currentCardDisplayedIndex]
                flashcardQuestion.text = question
                flashcardAnswer.text = answer
                flashcardAnswer2.text = wrongAnswer1
                flashcardAnswer3.text = wrongAnswer2

            } else {

                flashcardQuestion.text = ""
                flashcardAnswer.text = ""
            }
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer?.start()
    }
    var party= Party(
        speed = 0f,
        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
        position = Position.Relative(0.5, 0.3)
    )


    fun getRandomNumber(minNumber: Int, maxNumber: Int): Int {

    if (maxNumber == minNumber){
        return minNumber
    }
    var Randomnumb: Int
    do {
        Randomnumb =  (minNumber..maxNumber).random()
    } while (Randomnumb == currentCardDisplayedIndex)

    return Randomnumb
    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        if (data != null) {
            val question = data.getExtras()?.getString("question")
            val answer = data.getExtras()?.getString("answer1")
            val answer2 = data.getExtras()?.getString("answer2")
            val answer3 = data.getExtras()?.getString("answer3")



            Log.i("MainActivity", "question: $question")
            Log.i("MainActivity", "answer: $answer")
            Log.i("MainActivity", "answer: $answer2")
            Log.i("MainActivity", "answer: $answer3")



            findViewById<TextView>(R.id.flashcard_question).text = question
            findViewById<TextView>(R.id.flashcard_answer).text = answer
            findViewById<TextView>(R.id.flashcard_answer2).text = answer2
            findViewById<TextView>(R.id.flashcard_answer3).text = answer3

        } else {
            Log.i("MainActivity", "Returned null data from AddCardActivity")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var flashcardQuestion = findViewById<TextView>(R.id.flashcard_question)
        var flashcardAnswer = findViewById<TextView>(R.id.flashcard_answer)
        var flashcardAnswer2 = findViewById<TextView>(R.id.flashcard_answer2)
        var flashcardAnswer3 = findViewById<TextView>(R.id.flashcard_answer3)

        if (requestCode == ADD_CARD_REQUEST && resultCode == Activity.RESULT_OK) {
            val question = data?.getStringExtra("question")
            val answer = data?.getStringExtra("answer1")
            val answer2 = data?.getStringExtra("answer2")
            val answer3 = data?.getStringExtra("answer3")

            flashcardQuestion.text = question
            flashcardAnswer.text = answer
            flashcardAnswer2.text = answer2
            flashcardAnswer3.text = answer3

        }
    }
}
