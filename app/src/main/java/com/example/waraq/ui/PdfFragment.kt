package com.example.waraq.ui


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.MotionEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.addCallback
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.text.isDigitsOnly
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.waraq.R
import com.example.waraq.data.DrawingItem
import com.example.waraq.data.Notes
import com.example.waraq.data.PageNotes
import com.example.waraq.data.PaintData
import com.example.waraq.data.PathData
import com.example.waraq.data.Purchased
import com.example.waraq.databinding.FragmentPdfBinding
import com.example.waraq.util.CryptoManager
import com.example.waraq.util.KeyboardUtils
import com.example.waraq.viewModels.PdfViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.abs


class PdfFragment : BaseFragment<FragmentPdfBinding>(R.layout.fragment_pdf) {


    private val args: PdfFragmentArgs by navArgs()
    private lateinit var decryptedFile: File
    private lateinit var loadingPdfJob: Job
    private var isDrawing = false
    private var isHighlighting = false
    private val viewModel: PdfViewModel by viewModels()
    private var drawingPathsMap = HashMap<Int, MutableList<Pair<Path, Paint>>>()
    private var drawingPathsCoordinatesMap = HashMap<Int, MutableList<PathData>>()
    private var currentPage = 0
    private var notesMap = Notes(HashMap()).notes
    private var alpha = 95
    private var itemId = ""
    private val item by lazy { args.item }
    private var subtractOffset = 0f
    private var newPathCount = 0
    private var paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }


    override fun setup() {
        itemId = item.id

        loadingPdfJob = CoroutineScope(Dispatchers.IO).launch {
            viewModel.getDrawingItem(itemId)?.apply {
                this@PdfFragment.drawingPathsCoordinatesMap = this.pathMap
                this@PdfFragment.drawingPathsMap =
                    fromCoordinatesMapToPathMap(drawingPathsCoordinatesMap)
            }
            decryptPdf()
            yield()
            loadPdf()
        }


    }


    override fun addCallbacks() {

        setupDrawingTouchListener()

        binding.pageNumber.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearPageNumberFocus()
                true
            } else {
                false
            }
        }

        binding.pagesCount.setOnClickListener {
            binding.pageNumber.apply {
                setSelection(text.length)
                requestFocus()
            }
            KeyboardUtils.openKeyboard(requireContext(), binding.pageNumber)
        }

        KeyboardVisibilityEvent.setEventListener(requireActivity(),
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (!isOpen) {
                        clearPageNumberFocus()
                    }
                }
            })


        requireActivity().onBackPressedDispatcher.addCallback(this) {
            clearPageNumberFocus()

            if (binding.drawButton.isChecked)
                binding.drawButton.isChecked = false
            else if (binding.noteButton.isChecked)
                binding.noteButton.isChecked = false
            else if (binding.clearDrawButton.isChecked)
                binding.clearDrawButton.isChecked = false
            else if (binding.highlightButton.isChecked)
                binding.highlightButton.isChecked = false
            else
                findNavController().popBackStack()
        }

        viewModel.getAllNotes(itemId).observe(viewLifecycleOwner) {
            it?.apply { notesMap = notes }
        }


        binding.saveNote.setOnClickListener {
            binding.noteEt.text.toString().apply {
                if (isNotBlank()) {
                    notesMap[currentPage] = this
                    binding.noteAlert.visibility = VISIBLE
                } else {
                    notesMap.remove(currentPage)
                    binding.noteAlert.visibility = GONE
                }
                viewModel.saveNote(
                    PageNotes(
                        itemId,
                        Notes(notesMap)
                    )
                )
            }

            binding.addingNote = false
            binding.noteButton.isChecked = false
        }

        binding.pdfView.setOnClickListener {
            clearPageNumberFocus()
            if (!isDrawing || !isHighlighting) {
                binding.noteButton.isChecked = false
                binding.clearDrawButton.isChecked = false
            }
        }

        binding.colorsRadioGroup.setOnCheckedChangeListener { radioGroup: RadioGroup, id: Int ->
            paint.apply {
                color = (radioGroup.findViewById<RadioButton>(id)).text.toString().toInt()
                alpha = this@PdfFragment.alpha
            }
        }





        binding.brightnessButton.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
            binding.brightnessSeekBar.visibility = if (isChecked) VISIBLE else GONE
        }

        binding.brightnessSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                alpha = (60 + (.70 * progress)).toInt()
                println(alpha)
                paint.alpha = alpha
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        binding.undoButton.setOnClickListener {
            binding.newPathCount = --newPathCount
            drawingPathsMap[currentPage]?.apply { removeAt(lastIndex) }
            drawingPathsCoordinatesMap[currentPage]?.apply { removeAt(lastIndex) }
            binding.pdfView.invalidate()
            updateDrawingItem()
        }

        binding.noteButton.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                binding.addingNote = true
                setChecked(binding.noteButton.id)
                binding.noteEt.requestFocus()
            } else {
                binding.addingNote = false
            }
        }

        binding.drawButton.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                paint.alpha = 255
                calculateSubtractOffset()
                isDrawing = true
                binding.isDrawing = isDrawing
                setChecked(binding.drawButton.id)
                binding.pageNumber.isFocusable = false
                binding.pagesCount.isClickable = false
            } else {
                binding.pageNumber.isFocusableInTouchMode = true
                binding.pagesCount.isClickable = true
                isDrawing = false
                binding.isDrawing = isDrawing
                if (!isHighlighting) {
                    newPathCount = 0
                    binding.newPathCount = newPathCount
                }
            }
        }


        binding.highlightButton.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
            binding.brightnessButton.isChecked = false
            binding.editThickness.isChecked = false
            if (isChecked) {
                calculateSubtractOffset()
                isHighlighting = true
                binding.isHighlighting = isHighlighting
                binding.thick50.isChecked = true
                paint.alpha = alpha
                setChecked(binding.highlightButton.id)
                binding.pageNumber.isFocusable = false
                binding.pagesCount.isClickable = false
            } else {
                binding.pageNumber.isFocusableInTouchMode = true
                binding.pagesCount.isClickable = true
                isHighlighting = false
                binding.isHighlighting = isHighlighting

                if (!isDrawing) {
                    newPathCount = 0
                    binding.newPathCount = newPathCount
                }
            }
        }

        binding.clearDrawButton.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                setChecked(binding.clearDrawButton.id)
                binding.clearDrawLayout.visibility = VISIBLE
            } else {
                binding.clearDrawLayout.visibility = GONE
            }
        }

        binding.editThickness.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
            binding.thicknessRadioGroup.visibility = if (isChecked) VISIBLE else GONE
        }

        binding.thicknessRadioGroup.setOnCheckedChangeListener { radioGroup: RadioGroup, id: Int ->
            paint.strokeWidth = radioGroup.findViewById<RadioButton>(id).text.toString().toFloat()
            binding.editThickness.isChecked = false
        }

        binding.clearPage.setOnClickListener {
            drawingPathsMap.remove(currentPage)
            binding.pdfView.invalidate()
            drawingPathsCoordinatesMap.remove(currentPage)
            updateDrawingItem()
            binding.clearDrawButton.isChecked = false
        }

        binding.clearAllPages.setOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Clear All Pages")
                setMessage("All pages drawings Will be cleared press \"Clear All Pages\" to proceed.")
                setPositiveButton(
                    "Clear All Pages"
                ) { _, _ ->
                    drawingPathsMap.clear()
                    binding.pdfView.invalidate()
                    drawingPathsCoordinatesMap.clear()
                    updateDrawingItem()
                    binding.clearDrawButton.isChecked = false
                }
                setNegativeButton(
                    "Dismiss"
                ) { dialog, _ ->
                    dialog.dismiss()
                }
            }.show()

        }

        binding.noteAlert.setOnClickListener {
            if (binding.noteAlert.visibility == VISIBLE)
                binding.noteButton.isChecked = !binding.noteButton.isChecked
        }


        var pageNumber = 1
        binding.pageNumber.apply {
            doOnTextChanged { text, _, _, _ ->
                if (text?.isNotBlank() == true && text.isDigitsOnly() && pageNumber != text.toString()
                        .toInt() && binding.pageNumber.isFocused
                ) {
                    pageNumber = text.toString().toInt()
                    if (pageNumber <= binding.pdfView.pageCount) {
                        binding.pdfView.jumpTo(pageNumber - 1)
                    }
                }
            }
        }
    }

    private fun clearPageNumberFocus() {
        binding.pageNumber.apply {
            if (isFocused) {
                KeyboardUtils.closeKeyboard(requireContext(), binding.pageNumber)
                binding.pageNumber.clearFocus()
                binding.root.requestFocus()
                setText("${currentPage + 1}")
            }
        }
    }

    private fun loadPdf() {
        binding.pdfView
            .fromFile(decryptedFile)
            .enableDoubletap(false)
            .onLoad {
                binding.pdfView.maxZoom = 1f
                binding.isPdfLoaded = true
                binding.pagesCount.text = "/ ${binding.pdfView.pageCount}"
                decryptedFile.delete()
                if (item.isPurchased != Purchased.PURCHASED) {
                    initializeTimer()
                }
                binding.pageNumber.filters =
                    arrayOf<InputFilter>(LengthFilter(binding.pdfView.pageCount.toString().length));
            }

            .onDrawAll { canvas, pageWidth, pageHeight, displayedPage ->
                drawOnPage(canvas, pageWidth, pageHeight, displayedPage)
            }
            .onPageChange { currentPage: Int, pagesCount: Int ->
                this@PdfFragment.currentPage = currentPage
                binding.page.text = getString(R.string.page, "${currentPage + 1}")
                binding.clearPageTv.text = getString(R.string.clear_page, "${currentPage + 1}")
                binding.pageNumber.apply {
                    if (!isFocused) {
                        setText("${currentPage + 1}")
                    }
                    setSelection(text.length)
                }
                notesMap[currentPage].let { note ->
                    binding.noteEt.setText(note)

                    binding.noteAlert.visibility = if (note == null) {
                        GONE
                    } else {
                        binding.noteEt.setSelection(note.length)
                        VISIBLE
                    }

                }

            }
            .load()
    }

    private fun decryptPdf() {
        val encryptedFile = File(requireActivity().filesDir, item.title!!)
        decryptedFile = File.createTempFile("temp file", ".pdf", requireContext().cacheDir)
        CryptoManager().decryptFile(
            FileInputStream(encryptedFile),
            FileOutputStream(decryptedFile)
        )
    }

    private fun drawOnPage(
        canvas: Canvas?,
        pageWidth: Float,
        pageHeight: Float,
        displayedPage: Int,
    ) {
        val pathList = drawingPathsMap[displayedPage]
        if (canvas != null && pathList != null) {
            for (path in pathList) {
                val currentPath = Path(path.first)
                val matrix = Matrix().apply { setScale(pageWidth, pageHeight) }
                currentPath.transform(matrix)
                canvas.drawPath(currentPath, path.second)
            }
        }
    }

    private fun initializeTimer() {
        binding.timerLayout.visibility = VISIBLE
        var time = 20
        lifecycleScope.launch {
            while (true) {
                println(time)
                binding.timer.text = "00:${if (time < 10) "0$time" else time}"
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    time--
                }
                delay(1000)
                if (time == 0)
                    break
            }
//            findNavController().popBackStack()
        }
    }


    private fun preparePathData(pathList: MutableList<Pair<Float, Float>>): PathData {
        return PathData(pathList, getPaintDataFromPaint(paint))
    }

    private fun fromCoordinatesMapToPathMap(coordinatesMap: HashMap<Int, MutableList<PathData>>): HashMap<Int, MutableList<Pair<Path, Paint>>> {
        val pathMap = HashMap<Int, MutableList<Pair<Path, Paint>>>()
        coordinatesMap.forEach { item ->
            pathMap[item.key] = mutableListOf()
            item.value.forEach { pathData ->
                val path = Path()
                pathData.path.forEach { coordinates ->
                    val x = coordinates.first
                    val y = coordinates.second
                    if (path.isEmpty) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                pathMap[item.key]?.add(Pair(path, getPaintFromPaintData(pathData.paintData)))
            }
        }
        println(pathMap)
        return pathMap
    }

    private fun calculateSubtractOffset() {
        binding.pdfView.apply {
            val pageSize = getPageSize(currentPage)
            val scrolledPages = abs(currentYOffset) / pageSize.height
            val pagesOnScreen = currentPage - scrolledPages
            subtractOffset = pagesOnScreen * pageSize.height
        }
    }

    private var pathList = mutableListOf<Pair<Float, Float>>()

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDrawingTouchListener() {
        binding.drawLayout.setOnTouchListener { _, motionEvent: MotionEvent ->
            if (isDrawing || isHighlighting) {
                handleDrawingTouchEvent(motionEvent)
                true
            } else {
                false
            }
        }
    }

    private fun handleDrawingTouchEvent(motionEvent: MotionEvent) {
        binding.brightnessButton.isChecked = false
        binding.editThickness.isChecked = false
        val x = screenToPdfX(motionEvent.x)
        val y = screenToPdfY(motionEvent.y - subtractOffset)
        if (drawingPathsMap[currentPage] == null) {
            drawingPathsMap[currentPage] = mutableListOf()
            drawingPathsCoordinatesMap[currentPage] = mutableListOf()
        }
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                drawingPathsMap[currentPage]!!.add(Pair(Path(), Paint(paint)))
                drawingPathsMap[currentPage]!!.last().first.moveTo(x, y)
                pathList.add(Pair(x, y))
            }

            MotionEvent.ACTION_MOVE -> {
                pathList.add(Pair(x, y))
                drawingPathsMap[currentPage]!!.last().first.lineTo(x, y)
                binding.pdfView.invalidate()
            }

            MotionEvent.ACTION_UP -> {
                if (pathList.size > 5) {
                    binding.newPathCount = ++newPathCount
                    drawingPathsCoordinatesMap[currentPage]?.add(preparePathData(pathList))
                    pathList = mutableListOf()
                    updateDrawingItem()
                }
            }
        }
    }

    private fun screenToPdfX(x: Float): Float {
        return x / (binding.pdfView.getPageSize(currentPage).width)
    }

    private fun screenToPdfY(y: Float): Float {
        return y / (binding.pdfView.getPageSize(currentPage).height)
    }

    private fun getPaintDataFromPaint(paint: Paint): PaintData {
        return PaintData(color = paint.color, strokeWidth = paint.strokeWidth, alpha = paint.alpha)
    }

    private fun getPaintFromPaintData(paintData: PaintData): Paint {
        return Paint().apply {
            color = paintData.color
            strokeWidth = paintData.strokeWidth
            alpha = paintData.alpha
            style = Paint.Style.STROKE
            isAntiAlias = true

        }
    }

    private fun setChecked(id: Int) {
        binding.apply {
            if (id != drawButton.id) drawButton.isChecked = false
            if (id != highlightButton.id) highlightButton.isChecked = false
            if (id != clearDrawButton.id) clearDrawButton.isChecked = false
            if (id != noteButton.id) noteButton.isChecked = false
        }

    }

    private fun updateDrawingItem() {
        viewModel.upsertDrawingItem(
            DrawingItem(
                itemId,
                drawingPathsCoordinatesMap
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingPdfJob.cancel()
    }


}