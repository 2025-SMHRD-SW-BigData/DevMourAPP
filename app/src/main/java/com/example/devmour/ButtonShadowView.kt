package com.example // 실제 패키지명으로 바꾸세요

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button
import androidx.cardview.widget.CardView
import com.google.android.material.card.MaterialCardView

class ButtonShadowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val button: Button = Button(context)

    init {
        // CardView 속성
        radius = 12f
        cardElevation = 8f
        setCardBackgroundColor(Color.WHITE)
        isClickable = true
        isFocusable = true

        // 그림자 색상 (Android P 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            outlineSpotShadowColor = Color.parseColor("#2f354f")
        }

        // 버튼 속성
        button.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        button.setBackgroundColor(Color.TRANSPARENT)
        button.text = "버튼"
        addView(button)

        // 눌림 효과
        setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    cardElevation = 2f
                    scaleX = 0.98f
                    scaleY = 0.98f
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    cardElevation = 8f
                    scaleX = 1f
                    scaleY = 1f
                }
            }
            false
        }
    }

    // 버튼 텍스트 설정 함수
    fun setText(text: String) {
        button.text = text
    }

    // 버튼 클릭 리스너 설정
    fun setOnButtonClickListener(listener: OnClickListener) {
        button.setOnClickListener(listener)
    }
}