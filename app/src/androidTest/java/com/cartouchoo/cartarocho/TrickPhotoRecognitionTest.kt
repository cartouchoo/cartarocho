package com.cartouchoo.cartarocho

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cartouchoo.cartarocho.domain.TrickPhotoParser
import com.cartouchoo.cartarocho.ui.newround.readWordsFromPhoto
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Checks that the on-device text recognizer reads card indices well enough to be useful. */
@RunWith(AndroidJUnit4::class)
class TrickPhotoRecognitionTest {

    @Test
    fun readsTrumpNumbersAndHeadLetters(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "test-plis.jpg")
        drawFakeCards(listOf("21", "1", "14", "R", "D", "V")).compress(Bitmap.CompressFormat.JPEG, 95, file.outputStream())

        val words = readWordsFromPhoto(context, Uri.fromFile(file))
        val detection = TrickPhotoParser.parse(words)

        assertTrue("words read: $words", detection.trumps.containsAll(listOf(1, 14, 21)))
        assertEquals("words read: $words", 1, detection.rois)
        assertEquals("words read: $words", 1, detection.dames)
        assertEquals("words read: $words", 1, detection.valets)
        file.delete()
    }

    /** Draws one white "card" per index, side by side, like cards spread on a table. */
    private fun drawFakeCards(indices: List<String>): Bitmap {
        val cardW = 300
        val cardH = 460
        val bitmap = Bitmap.createBitmap(cardW * indices.size + 40, cardH + 40, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(20, 90, 50))
        val card = Paint().apply { color = Color.WHITE }
        val text = Paint().apply {
            color = Color.BLACK
            textSize = 90f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        indices.forEachIndexed { i, index ->
            val left = 20f + i * cardW
            canvas.drawRect(left + 10, 20f, left + cardW - 10, 20f + cardH, card)
            canvas.drawText(index, left + 40, 140f, text)
        }
        return bitmap
    }
}
