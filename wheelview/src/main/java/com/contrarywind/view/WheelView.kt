package com.contrarywind.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import com.contrarywind.adapter.WheelAdapter
import com.contrarywind.interfaces.IPickerViewData
import com.contrarywind.listener.LoopViewGestureListener
import com.contrarywind.listener.OnItemSelectedListener
import com.contrarywind.timer.InertiaTimerTask
import com.contrarywind.timer.MessageHandler
import com.contrarywind.timer.SmoothScrollTimerTask
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.acos

/**
 * 3d滚轮控件
 */
class WheelView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    enum class ACTION {
        // 点击，滑翔(滑到尽头)，拖拽事件
        CLICK, FLING, DAGGLE
    }

    enum class DividerType {
        // 分隔线类型
        FILL, WRAP, CIRCLE
    }

    private var dividerType: DividerType? = null //分隔线类型
    private var context: Context? = null
    private var handler: Handler? = null
    private var gestureDetector: GestureDetector? = null
    private var onItemSelectedListener: OnItemSelectedListener? = null
    private var isOptions = false
    private var isCenterLabel = true

    // Timer mTimer;
    private val mExecutor = Executors.newSingleThreadScheduledExecutor()
    private var mFuture: ScheduledFuture<*>? = null
    private var paintOuterText: Paint? = null
    private var paintCenterText: Paint? = null
    private var paintIndicator: Paint? = null
    private var adapter: WheelAdapter<*>? = null
    private var label: String? = null //附加单位
    private var textSize //选项的文字大小
            : Int
    private var maxTextWidth = 0
    private var maxTextHeight = 0
    private var textXOffset = 0
    var itemHeight = 0f //每行高度
        private set
    private var typeface = Typeface.MONOSPACE //字体样式，默认是等宽字体
    private var textColorOut = 0
    private var textColorCenter = 0
    private var dividerColor = 0
    private var dividerWidth = 0

    // 条目间距倍数
    private var lineSpacingMultiplier = 1.6f
    var isLoop = false
        private set

    // 第一条线Y坐标值
    private var firstLineY = 0f

    //第二条线Y坐标
    private var secondLineY = 0f

    //中间label绘制的Y坐标
    private var centerY = 0f

    //当前滚动总高度y值
    var totalScrollY = 0f

    //初始化默认选中项
    var initPosition = 0
        private set

    //选中的Item是第几个
    private var selectedItem = 0
    private var preCurrentIndex = 0

    // 绘制几个条目，实际上第一项和最后一项Y轴压缩成0%了，所以可见的数目实际为9
    private var itemsVisible = 11
    private var measuredHeight = 0 // WheelView 控件高度
    private var measuredWidth = 0 // WheelView 控件宽度

    // 半径
    private var radius = 0
    private var mOffset = 0
    private var previousY = 0f
    private var startTime: Long = 0
    private var widthMeasureSpec = 0
    private var mGravity = Gravity.CENTER
    private var drawCenterContentStart = 0 //中间选中文字开始绘制位置
    private var drawOutContentStart = 0 //非中间文字开始绘制位置
    private var CENTER_CONTENT_OFFSET = 0f //偏移量
    private var isAlphaGradient = false //透明度渐变

    init {
        textSize = resources.getDimensionPixelSize(R.dimen.pickerview_textsize) //默认大小
        val dm = resources.displayMetrics
        val density = dm.density // 屏幕密度比（0.75/1.0/1.5/2.0/3.0）
        if (density < 1) { //根据密度不同进行适配
            CENTER_CONTENT_OFFSET = 2.4f
        } else if (1 <= density && density < 2) {
            CENTER_CONTENT_OFFSET = 4.0f
        } else if (2 <= density && density < 3) {
            CENTER_CONTENT_OFFSET = 6.0f
        } else if (density >= 3) {
            CENTER_CONTENT_OFFSET = density * 2.5f
        }
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.WheelView, 0, 0)
            mGravity = a.getInt(R.styleable.WheelView_wv_gravity, Gravity.CENTER)
            textColorOut = a.getColor(R.styleable.WheelView_wv_textColorOut, -0x575758)
            textColorCenter =
                a.getColor(R.styleable.WheelView_wv_textColorCenter, -0xd5d5d6)
            dividerColor = a.getColor(R.styleable.WheelView_wv_dividerColor, -0x2a2a2b)
            dividerWidth = a.getDimensionPixelSize(R.styleable.WheelView_wv_dividerWidth, 2)
            textSize =
                a.getDimensionPixelOffset(R.styleable.WheelView_wv_textSize, textSize)
            lineSpacingMultiplier = a.getFloat(R.styleable.WheelView_wv_lineSpacingMultiplier,
                lineSpacingMultiplier
            )
            a.recycle() //回收内存
        }
        judgeLineSpace()
        initLoopView(context)
    }

    /**
     * 判断间距是否在1.0-4.0之间
     */
    private fun judgeLineSpace() {
        if (lineSpacingMultiplier < 1.0f) {
            lineSpacingMultiplier = 1.0f
        } else if (lineSpacingMultiplier > 4.0f) {
            lineSpacingMultiplier = 4.0f
        }
    }

    private fun initLoopView(context: Context) {
        this.context = context
        handler = MessageHandler(this)
        gestureDetector = GestureDetector(context, LoopViewGestureListener(this))
        gestureDetector!!.setIsLongpressEnabled(false)
        isLoop = true
        totalScrollY = 0f
        initPosition = -1
        initPaints()
    }

    private fun initPaints() {
        paintOuterText = Paint()
        paintOuterText!!.color = textColorOut
        paintOuterText!!.isAntiAlias = true
        paintOuterText!!.typeface = typeface
        paintOuterText!!.textSize = textSize.toFloat()
        paintCenterText = Paint()
        paintCenterText!!.color = textColorCenter
        paintCenterText!!.isAntiAlias = true
        paintCenterText!!.textScaleX = 1.1f
        paintCenterText!!.typeface = typeface
        paintCenterText!!.textSize = textSize.toFloat()
        paintIndicator = Paint()
        paintIndicator!!.color = dividerColor
        paintIndicator!!.isAntiAlias = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private fun reMeasure() { //重新测量
        if (adapter == null) {
            return
        }
        measureTextWidthHeight()

        //半圆的周长 = item高度乘以item数目-1
        val halfCircumference = (itemHeight * (itemsVisible - 1)).toInt()
        //整个圆的周长除以PI得到直径，这个直径用作控件的总高度
        measuredHeight = (halfCircumference * 2 / Math.PI).toInt()
        //求出半径
        radius = (halfCircumference / Math.PI).toInt()
        //控件宽度，这里支持weight
        measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        //计算两条横线 和 选中项画笔的基线Y位置
        firstLineY = (measuredHeight - itemHeight) / 2.0f
        secondLineY = (measuredHeight + itemHeight) / 2.0f
        centerY = secondLineY - (itemHeight - maxTextHeight) / 2.0f - CENTER_CONTENT_OFFSET

        //初始化显示的item的position
        if (initPosition == -1) {
            initPosition = if (isLoop) {
                (adapter!!.itemsCount() + 1) / 2
            } else {
                0
            }
        }
        preCurrentIndex = initPosition
    }

    /**
     * 计算最大length的Text的宽高度
     */
    private fun measureTextWidthHeight() {
        val rect = Rect()
        for (i in 0 until adapter!!.itemsCount()) {
            val s1 = getContentText(adapter!!.getItem(i))
            paintCenterText!!.getTextBounds(s1, 0, s1.length, rect)
            val textWidth = rect.width()
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth
            }
        }
        paintCenterText!!.getTextBounds("\u661F\u671F", 0, 2, rect) // 星期的字符编码（以它为标准高度）
        maxTextHeight = rect.height() + 2
        itemHeight = lineSpacingMultiplier * maxTextHeight
    }

    fun smoothScroll(action: ACTION) { //平滑滚动的实现
        cancelFuture()
        if (action == ACTION.FLING || action == ACTION.DAGGLE) {
            mOffset = ((totalScrollY % itemHeight + itemHeight) % itemHeight).toInt()
            mOffset = if (mOffset.toFloat() > itemHeight / 2.0f) { //如果超过Item高度的一半，滚动到下一个Item去
                (itemHeight - mOffset.toFloat()).toInt()
            } else {
                -mOffset
            }
        }
        //停止的时候，位置有偏移，不是全部都能正确停止到中间位置的，这里把文字位置挪回中间去
        mFuture = mExecutor.scheduleWithFixedDelay(
            SmoothScrollTimerTask(this, mOffset),
            0,
            10,
            TimeUnit.MILLISECONDS
        )
    }

    fun scrollBy(velocityY: Float) { //滚动惯性的实现
        cancelFuture()
        mFuture = mExecutor.scheduleWithFixedDelay(
            InertiaTimerTask(this, velocityY),
            0,
            VELOCITY_FLING.toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    fun cancelFuture() {
        if (mFuture != null && !mFuture!!.isCancelled) {
            mFuture!!.cancel(true)
            mFuture = null
        }
    }

    /**
     * 设置是否循环滚动
     *
     * @param cyclic 是否循环
     */
    fun setCyclic(cyclic: Boolean) {
        isLoop = cyclic
    }

    fun setTypeface(font: Typeface) {
        typeface = font
        paintOuterText!!.typeface = typeface
        paintCenterText!!.typeface = typeface
    }

    fun setTextSize(size: Float) {
        if (size > 0.0f) {
            textSize = (context!!.resources.displayMetrics.density * size).toInt()
            paintOuterText!!.textSize = textSize.toFloat()
            paintCenterText!!.textSize = textSize.toFloat()
        }
    }

    fun setOnItemSelectedListener(OnItemSelectedListener: OnItemSelectedListener) {
        onItemSelectedListener = OnItemSelectedListener
    }

    fun setAdapter(adapter: WheelAdapter<*>?) {
        this.adapter = adapter
        reMeasure()
        invalidate()
    }

    fun setItemsVisibleCount(visibleCount: Int) {
        var vc = visibleCount
        if (vc % 2 == 0) {
            vc += 1
        }
        itemsVisible = vc + 2 //第一条和最后一条
    }

    fun setAlphaGradient(alphaGradient: Boolean) {
        isAlphaGradient = alphaGradient
    }

    fun getAdapter(): WheelAdapter<*>? {
        return adapter
    }

    var currentItem: Int
        get() {
            // return selectedItem;
            if (adapter == null) {
                return 0
            }
            return if (isLoop && (selectedItem < 0 || selectedItem >= adapter!!.itemsCount())) {
                0.coerceAtLeast(abs(abs(selectedItem) - adapter!!.itemsCount())
                        .coerceAtMost(adapter!!.itemsCount() - 1)
                )
            } else 0.coerceAtLeast(selectedItem.coerceAtMost(adapter!!.itemsCount() - 1))
        }
        set(currentItem) {
            //不添加这句,当这个wheelView不可见时,默认都是0,会导致获取到的时间错误
            selectedItem = currentItem
            initPosition = currentItem
            totalScrollY = 0f //回归顶部，不然重设setCurrentItem的话位置会偏移的，就会显示出不对位置的数据
            invalidate()
        }

    fun onItemSelected() {
        if (onItemSelectedListener != null) {
            postDelayed({
                onItemSelectedListener!!.onItemSelected(currentItem)
                 }, 200L)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (adapter == null) {
            return
        }
        //initPosition越界会造成preCurrentIndex的值不正确
        initPosition = 0.coerceAtLeast(initPosition).coerceAtMost(adapter!!.itemsCount() - 1)

        //滚动的Y值高度除去每行Item的高度，得到滚动了多少个item，即change数
        //滚动偏移值,用于记录滚动了多少个item
        val change = (totalScrollY / itemHeight).toInt()
        // Log.d("change", "" + change);
        try {
            //滚动中实际的预选中的item(即经过了中间位置的item) ＝ 滑动前的位置 ＋ 滑动相对位置
            preCurrentIndex = initPosition + change % adapter!!.itemsCount()
        } catch (e: ArithmeticException) {
            Log.e("WheelView", "出错了！adapter.getItemsCount() == 0，联动数据不匹配")
        }
        if (!isLoop) { //不循环的情况
            if (preCurrentIndex < 0) {
                preCurrentIndex = 0
            }
            if (preCurrentIndex > adapter!!.itemsCount() - 1) {
                preCurrentIndex = adapter!!.itemsCount() - 1
            }
        } else { //循环
            if (preCurrentIndex < 0) { //举个例子：如果总数是5，preCurrentIndex ＝ －1，那么preCurrentIndex按循环来说，其实是0的上面，也就是4的位置
                preCurrentIndex += adapter!!.itemsCount()
            }
            if (preCurrentIndex > adapter!!.itemsCount() - 1) { //同理上面,自己脑补一下
                preCurrentIndex -= adapter!!.itemsCount()
            }
        }
        //跟滚动流畅度有关，总滑动距离与每个item高度取余，即并不是一格格的滚动，每个item不一定滚到对应Rect里的，这个item对应格子的偏移值
        val itemHeightOffset = totalScrollY % itemHeight


        //绘制中间两条横线
        if (dividerType == DividerType.WRAP) { //横线长度仅包裹内容
            var startX: Float
            val endX: Float
            startX = if (TextUtils.isEmpty(label)) { //隐藏Label的情况
                ((measuredWidth - maxTextWidth) / 2 - 12).toFloat()
            } else {
                ((measuredWidth - maxTextWidth) / 4 - 12).toFloat()
            }
            if (startX <= 0) { //如果超过了WheelView的边缘
                startX = 10f
            }
            endX = measuredWidth - startX
            canvas.drawLine(startX, firstLineY, endX, firstLineY, paintIndicator!!)
            canvas.drawLine(startX, secondLineY, endX, secondLineY, paintIndicator!!)
        } else if (dividerType == DividerType.CIRCLE) {
            //分割线为圆圈形状
            paintIndicator!!.style = Paint.Style.STROKE
            paintIndicator!!.strokeWidth = dividerWidth.toFloat()
            var startX: Float
            val endX: Float
            startX = if (TextUtils.isEmpty(label)) { //隐藏Label的情况
                (measuredWidth - maxTextWidth) / 2f - 12
            } else {
                (measuredWidth - maxTextWidth) / 4f - 12
            }
            if (startX <= 0) { //如果超过了WheelView的边缘
                startX = 10f
            }
            endX = measuredWidth - startX
            //半径始终以宽高中最大的来算
            val radius = Math.max(endX - startX, itemHeight) / 1.8f
            canvas.drawCircle(measuredWidth / 2f, measuredHeight / 2f, radius, paintIndicator!!)
        } else {
            canvas.drawLine(0.0f, firstLineY, measuredWidth.toFloat(), firstLineY, paintIndicator!!)
            canvas.drawLine(
                0.0f,
                secondLineY,
                measuredWidth.toFloat(),
                secondLineY,
                paintIndicator!!
            )
        }

        //只显示选中项Label文字的模式，并且Label文字不为空，则进行绘制
        if (!TextUtils.isEmpty(label) && isCenterLabel) {
            //绘制文字，靠右并留出空隙
            val drawRightContentStart = measuredWidth - getTextWidth(paintCenterText, label)
            canvas.drawText(
                label!!,
                drawRightContentStart - CENTER_CONTENT_OFFSET,
                centerY,
                paintCenterText!!
            )
        }

        // 设置数组中每个元素的值
        var counter = 0
        while (counter < itemsVisible) {
            var showText: Any?
            var index =
                preCurrentIndex - (itemsVisible / 2 - counter) //索引值，即当前在控件中间的item看作数据源的中间，计算出相对源数据源的index值

            //判断是否循环，如果是循环数据源也使用相对循环的position获取对应的item值，如果不是循环则超出数据源范围使用""空白字符串填充，在界面上形成空白无数据的item项
            if (isLoop) {
                index = getLoopMappingIndex(index)
                showText = adapter!!.getItem(index)
            } else if (index < 0) {
                showText = ""
            } else if (index > adapter!!.itemsCount() - 1) {
                showText = ""
            } else {
                showText = adapter!!.getItem(index)
            }
            canvas.save()
            // 弧长 L = itemHeight * counter - itemHeightOffset
            // 求弧度 α = L / r  (弧长/半径) [0,π]
            val radian = ((itemHeight * counter - itemHeightOffset) / radius).toDouble()
            // 弧度转换成角度(把半圆以Y轴为轴心向右转90度，使其处于第一象限及第四象限
            // angle [-90°,90°]
            val angle = (90.0 - radian / Math.PI * 180.0).toFloat() //item第一项,从90度开始，逐渐递减到 -90度

            // 计算取值可能有细微偏差，保证负90°到90°以外的不绘制
            if (angle > 90f || angle < -90f) {
                canvas.restore()
            } else {
                //获取内容文字
                var contentText: String

                //如果是label每项都显示的模式，并且item内容不为空、label 也不为空
                contentText = if (!isCenterLabel && !TextUtils.isEmpty(label) && !TextUtils.isEmpty(
                        getContentText(showText)
                    )
                ) {
                    getContentText(showText) + label
                } else {
                    getContentText(showText)
                }
                // 根据当前角度计算出偏差系数，用以在绘制时控制文字的 水平移动 透明度 倾斜程度.
                val offsetCoefficient = Math.pow((Math.abs(angle) / 90f).toDouble(), 2.2).toFloat()
                reMeasureTextSize(contentText)
                //计算开始绘制的位置
                measuredCenterContentStart(contentText)
                measuredOutContentStart(contentText)
                val translateY =
                    (radius - Math.cos(radian) * radius - Math.sin(radian) * maxTextHeight / 2.0).toFloat()
                //根据Math.sin(radian)来更改canvas坐标系原点，然后缩放画布，使得文字高度进行缩放，形成弧形3d视觉差
                canvas.translate(0.0f, translateY)
                if (translateY <= firstLineY && maxTextHeight + translateY >= firstLineY) {
                    // 条目经过第一条线
                    canvas.save()
                    canvas.clipRect(0f, 0f, measuredWidth.toFloat(), firstLineY - translateY)
                    canvas.scale(1.0f, Math.sin(radian).toFloat() * SCALE_CONTENT)
                    setOutPaintStyle(offsetCoefficient, angle)
                    canvas.drawText(
                        contentText,
                        drawOutContentStart.toFloat(),
                        maxTextHeight.toFloat(),
                        paintOuterText!!
                    )
                    canvas.restore()
                    canvas.save()
                    canvas.clipRect(
                        0f, firstLineY - translateY, measuredWidth.toFloat(), itemHeight.toInt()
                            .toFloat()
                    )
                    canvas.scale(1.0f, Math.sin(radian).toFloat() * 1.0f)
                    canvas.drawText(
                        contentText,
                        drawCenterContentStart.toFloat(),
                        maxTextHeight - CENTER_CONTENT_OFFSET,
                        paintCenterText!!
                    )
                    canvas.restore()
                } else if (translateY <= secondLineY && maxTextHeight + translateY >= secondLineY) {
                    // 条目经过第二条线
                    canvas.save()
                    canvas.clipRect(0f, 0f, measuredWidth.toFloat(), secondLineY - translateY)
                    canvas.scale(1.0f, Math.sin(radian).toFloat() * 1.0f)
                    canvas.drawText(
                        contentText,
                        drawCenterContentStart.toFloat(),
                        maxTextHeight - CENTER_CONTENT_OFFSET,
                        paintCenterText!!
                    )
                    canvas.restore()
                    canvas.save()
                    canvas.clipRect(
                        0f, secondLineY - translateY, measuredWidth.toFloat(), itemHeight.toInt()
                            .toFloat()
                    )
                    canvas.scale(1.0f, Math.sin(radian).toFloat() * SCALE_CONTENT)
                    setOutPaintStyle(offsetCoefficient, angle)
                    canvas.drawText(
                        contentText,
                        drawOutContentStart.toFloat(),
                        maxTextHeight.toFloat(),
                        paintOuterText!!
                    )
                    canvas.restore()
                } else if (translateY >= firstLineY && maxTextHeight + translateY <= secondLineY) {
                    // 中间条目
                    // canvas.clipRect(0, 0, measuredWidth, maxTextHeight);
                    //让文字居中
                    val Y =
                        maxTextHeight - CENTER_CONTENT_OFFSET //因为圆弧角换算的向下取值，导致角度稍微有点偏差，加上画笔的基线会偏上，因此需要偏移量修正一下
                    canvas.drawText(
                        contentText,
                        drawCenterContentStart.toFloat(),
                        Y,
                        paintCenterText!!
                    )
                    //设置选中项
                    selectedItem = preCurrentIndex - (itemsVisible / 2 - counter)
                } else {
                    // 其他条目
                    canvas.save()
                    canvas.clipRect(0, 0, measuredWidth, itemHeight.toInt())
                    canvas.scale(1.0f, Math.sin(radian).toFloat() * SCALE_CONTENT)
                    setOutPaintStyle(offsetCoefficient, angle)
                    // 控制文字水平偏移距离
                    canvas.drawText(
                        contentText,
                        drawOutContentStart + textXOffset * offsetCoefficient,
                        maxTextHeight.toFloat(),
                        paintOuterText!!
                    )
                    canvas.restore()
                }
                canvas.restore()
                paintCenterText!!.textSize = textSize.toFloat()
            }
            counter++
        }
    }

    //设置文字倾斜角度，透明度
    private fun setOutPaintStyle(offsetCoefficient: Float, angle: Float) {
        // 控制文字倾斜角度
        val DEFAULT_TEXT_TARGET_SKEW_X = 0.5f
        var multiplier = 0
        if (textXOffset > 0) {
            multiplier = 1
        } else if (textXOffset < 0) {
            multiplier = -1
        }
        paintOuterText!!.textSkewX =
            multiplier * (if (angle > 0) -1 else 1) * DEFAULT_TEXT_TARGET_SKEW_X * offsetCoefficient

        // 控制透明度
        val alpha = if (isAlphaGradient) ((90f - Math.abs(angle)) / 90f * 255).toInt() else 255
        // Log.d("WheelView", "alpha:" + alpha);
        paintOuterText!!.alpha = alpha
    }

    /**
     * reset the size of the text Let it can fully display
     *
     * @param contentText item text content.
     */
    private fun reMeasureTextSize(contentText: String) {
        val rect = Rect()
        paintCenterText!!.getTextBounds(contentText, 0, contentText.length, rect)
        var width = rect.width()
        var size = textSize
        while (width > measuredWidth) {
            size--
            //设置2条横线中间的文字大小
            paintCenterText!!.textSize = size.toFloat()
            paintCenterText!!.getTextBounds(contentText, 0, contentText.length, rect)
            width = rect.width()
        }
        //设置2条横线外面的文字大小
        paintOuterText!!.textSize = size.toFloat()
    }

    //递归计算出对应的index
    private fun getLoopMappingIndex(index: Int): Int {
        var index1 = index
        if (index1 < 0) {
            index1 += adapter!!.itemsCount()
            index1 = getLoopMappingIndex(index1)
        } else if (index1 > adapter!!.itemsCount() - 1) {
            index1 -= adapter!!.itemsCount()
            index1 = getLoopMappingIndex(index1)
        }
        return index1
    }

    /**
     * 获取所显示的数据源
     *
     * @param item data resource
     * @return 对应显示的字符串
     */
    private fun getContentText(item: Any?): String {
        if (item == null) {
            return ""
        } else if (item is IPickerViewData) {
            return item.pickerViewText()!!
        } else if (item is Int) {
            //如果为整形则最少保留两位数.
            return getFixNum(item)
        }
        return item.toString()
    }

    private fun getFixNum(timeNum: Int): String {
        return if (timeNum in 0..9) TIME_NUM[timeNum] else timeNum.toString()
    }

    private fun measuredCenterContentStart(content: String) {
        val rect = Rect()
        paintCenterText!!.getTextBounds(content, 0, content.length, rect)
        when (mGravity) {
            Gravity.CENTER -> drawCenterContentStart =
                if (isOptions || label == null || label == "" || !isCenterLabel) {
                    ((measuredWidth - rect.width()) * 0.5).toInt()
                } else { //只显示中间label时，时间选择器内容偏左一点，留出空间绘制单位标签
                    ((measuredWidth - rect.width()) * 0.25).toInt()
                }

            Gravity.LEFT -> drawCenterContentStart = 0
            Gravity.RIGHT -> drawCenterContentStart =
                measuredWidth - rect.width() - CENTER_CONTENT_OFFSET.toInt()
        }
    }

    private fun measuredOutContentStart(content: String) {
        val rect = Rect()
        paintOuterText!!.getTextBounds(content, 0, content.length, rect)
        when (mGravity) {
            Gravity.CENTER -> drawOutContentStart =
                if (isOptions || label == null || label == "" || !isCenterLabel) {
                    ((measuredWidth - rect.width()) * 0.5).toInt()
                } else { //只显示中间label时，时间选择器内容偏左一点，留出空间绘制单位标签
                    ((measuredWidth - rect.width()) * 0.25).toInt()
                }

            Gravity.LEFT -> drawOutContentStart = 0
            Gravity.RIGHT -> drawOutContentStart =
                measuredWidth - rect.width() - CENTER_CONTENT_OFFSET.toInt()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        this.widthMeasureSpec = widthMeasureSpec
        reMeasure()
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventConsumed = gestureDetector!!.onTouchEvent(event)
        var isIgnore = false //超过边界滑动时，不再绘制UI。
        val top = -initPosition * itemHeight
        val bottom = (adapter!!.itemsCount() - 1 - initPosition) * itemHeight
        val ratio = 0.25f
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startTime = System.currentTimeMillis()
                cancelFuture()
                previousY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = previousY - event.rawY
                previousY = event.rawY
                totalScrollY += dy

                // normal mode。
                if (!isLoop) {
                    if (totalScrollY - itemHeight * ratio < top && dy < 0 || totalScrollY + itemHeight * ratio > bottom && dy > 0) {
                        //快滑动到边界了，设置已滑动到边界的标志
                        totalScrollY -= dy
                        isIgnore = true
                    } else {
                        isIgnore = false
                    }
                }
            }

            MotionEvent.ACTION_UP -> if (!eventConsumed) { //未消费掉事件
                /**
                 * @describe <关于弧长的计算>
                 *
                 * 弧长公式： L = α*R
                 * 反余弦公式：arccos(cosα) = α
                 * 由于之前是有顺时针偏移90度，
                 * 所以实际弧度范围α2的值 ：α2 = π/2-α    （α=[0,π] α2 = [-π/2,π/2]）
                 * 根据正弦余弦转换公式 cosα = sin(π/2-α)
                 * 代入，得： cosα = sin(π/2-α) = sinα2 = (R - y) / R
                 * 所以弧长 L = arccos(cosα)*R = arccos((R - y) / R)*R
                </关于弧长的计算> */
                val y = event.y
                val lo = acos(((radius - y) / radius).toDouble()) * radius
                //item0 有一半是在不可见区域，所以需要加上 itemHeight / 2
                val circlePosition = ((lo + itemHeight / 2) / itemHeight).toInt()
                val extraOffset = (totalScrollY % itemHeight + itemHeight) % itemHeight
                //已滑动的弧长值
                mOffset = ((circlePosition - itemsVisible / 2) * itemHeight - extraOffset).toInt()
                if (System.currentTimeMillis() - startTime > 120) {
                    // 处理拖拽事件
                    smoothScroll(ACTION.DAGGLE)
                } else {
                    // 处理条目点击事件
                    smoothScroll(ACTION.CLICK)
                }
            }

            else -> if (!eventConsumed) {
                val y = event.y
                val L = Math.acos(((radius - y) / radius).toDouble()) * radius
                val circlePosition = ((L + itemHeight / 2) / itemHeight).toInt()
                val extraOffset = (totalScrollY % itemHeight + itemHeight) % itemHeight
                mOffset = ((circlePosition - itemsVisible / 2) * itemHeight - extraOffset).toInt()
                if (System.currentTimeMillis() - startTime > 120) {
                    smoothScroll(ACTION.DAGGLE)
                } else {
                    smoothScroll(ACTION.CLICK)
                }
            }
        }
        if (!isIgnore && event.action != MotionEvent.ACTION_DOWN) {
            invalidate()
        }
        return true
    }

    val itemsCount: Int
        get() = if (adapter != null) adapter!!.itemsCount() else 0

    fun setLabel(label: String?) {
        this.label = label
    }

    fun isCenterLabel(isCenterLabel: Boolean) {
        this.isCenterLabel = isCenterLabel
    }

    fun setGravity(gravity: Int) {
        mGravity = gravity
    }

    fun getTextWidth(paint: Paint?, str: String?): Int { //calculate text width
        var iRet = 0
        if (str != null && str.length > 0) {
            val len = str.length
            val widths = FloatArray(len)
            paint!!.getTextWidths(str, widths)
            for (j in 0 until len) {
                iRet += Math.ceil(widths[j].toDouble()).toInt()
            }
        }
        return iRet
    }

    fun setIsOptions(options: Boolean) {
        isOptions = options
    }

    fun setTextColorOut(textColorOut: Int) {
        this.textColorOut = textColorOut
        paintOuterText!!.color = this.textColorOut
    }

    fun setTextColorCenter(textColorCenter: Int) {
        this.textColorCenter = textColorCenter
        paintCenterText!!.color = this.textColorCenter
    }

    fun setTextXOffset(textXOffset: Int) {
        this.textXOffset = textXOffset
        if (textXOffset != 0) {
            paintCenterText!!.textScaleX = 1.0f
        }
    }

    fun setDividerWidth(dividerWidth: Int) {
        this.dividerWidth = dividerWidth
        paintIndicator!!.strokeWidth = dividerWidth.toFloat()
    }

    fun setDividerColor(dividerColor: Int) {
        this.dividerColor = dividerColor
        paintIndicator!!.color = dividerColor
    }

    fun setDividerType(dividerType: DividerType?) {
        this.dividerType = dividerType
    }

    fun setLineSpacingMultiplier(lineSpacingMultiplier: Float) {
        if (lineSpacingMultiplier != 0f) {
            this.lineSpacingMultiplier = lineSpacingMultiplier
            judgeLineSpace()
        }
    }

    override fun getHandler(): Handler {
        return handler!!
    }

    companion object {
        private val TIME_NUM = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09")

        // 修改这个值可以改变滑行速度
        private const val VELOCITY_FLING = 5
        private const val SCALE_CONTENT = 0.8f //非中间文字则用此控制高度，压扁形成3d错觉
    }
}