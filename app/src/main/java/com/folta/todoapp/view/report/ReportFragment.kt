package com.folta.todoapp.view.report

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.folta.todoapp.R
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.fragment_report.*
import org.threeten.bp.LocalDate
import java.text.DecimalFormat
import kotlin.collections.ArrayList


class ReportFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        Toast.makeText(
            this.context,
            "You picked the following date: " + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year,
            Toast.LENGTH_LONG
        ).show()
    }

//    private var mTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val now = LocalDate.now()
        val dpd = DatePickerDialog.newInstance(
            this,
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        )
        fragmentManager?.let { dpd.show(it, "Datepickerdialog") }
// If you're calling this from an AppCompatActivity
// dpd.show(getSupportFragmentManager(), "Datepickerdialog");

//        barChart.apply {
//            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//                override fun onValueSelected(e: Entry?, h: Highlight?) {
//                    Logger.d("Entry selected" + e.toString())
//                }
//
//                override fun onNothingSelected() {
//                    Logger.d("Nothing selected")
//                }
//            })
//        }
        val entry = ArrayList<BarEntry>()
        entry.add(BarEntry(1f, 10f))
        entry.add(BarEntry(2f, 20f))
        entry.add(BarEntry(3f, 30f))
        entry.add(BarEntry(4f, 40f))
        entry.add(BarEntry(5f, 50f))
        entry.add(BarEntry(6f, 60f))
        entry.add(BarEntry(7f, 70f))
        entry.add(BarEntry(8f, 80f))
        entry.add(BarEntry(9f, 90f))
        entry.add(BarEntry(10f, 100f))

        val dataset = BarDataSet(entry, "データ表示")
        context?.let { dataset.color = getColor(it, R.color.colorLine) }
        val datasetlist = mutableListOf(dataset)
        val data = BarData(datasetlist as List<IBarDataSet>?)
        data.setValueFormatter(object : ValueFormatter() {
            private val format = DecimalFormat("####")
            override fun getBarLabel(barEntry: BarEntry?): String {
                return format.format(barEntry?.y)
            }

            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return format.format(value)
            }
        })

        data.setValueTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL))
        data.setValueTextSize(16f)
        barChart.data = data

        val xLabels = arrayOf("", "国語", "数学", "英語") //最初の””は原点の値

        //Y軸(左)の設定
        barChart.axisLeft.apply {
            //            axisMinimum = 0f
//            axisMaximum = 120f
//            labelCount = 4
            setDrawTopYLabelEntry(false)
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setDrawLabels(false)
        }

        //Y軸(右)の設定
        barChart.axisRight.apply {
            setDrawLabels(false)
            setDrawGridLines(false)
            setDrawZeroLine(false)
            setDrawAxisLine(false)
            setDrawTopYLabelEntry(false)
        }

        //X軸の設定
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(xLabels)
            labelCount = 10 //表示させるラベル数
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
            axisLineWidth = 1f
            textSize = 12f

            setDrawGridLines(false)
            setDrawAxisLine(true)
        }

        //グラフ上の表示
        barChart.apply {
            description.isEnabled = false
//            isClickable = false
            legend.isEnabled = false //凡例
            setDrawBorders(true)
            setExtraOffsets(30f, 30f, 30f, 30f)
            setBorderColor(getColor(context, R.color.colorBase))
            setScaleEnabled(false)
            setDrawValueAboveBar(true)
            setTouchEnabled(false)
            animateY(600, Easing.EaseOutCirc)
            invalidate()
        }
    }
}

