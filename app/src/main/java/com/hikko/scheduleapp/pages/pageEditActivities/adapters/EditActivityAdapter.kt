package com.hikko.scheduleapp.pages.pageEditActivities.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.ViewTreeObserver.OnPreDrawListener
import android.widget.*
import com.daimajia.swipe.adapters.ArraySwipeAdapter
import com.hikko.scheduleapp.utilClasses.Activity
import com.hikko.scheduleapp.ActivityUtils.clearInputFocus
import com.hikko.scheduleapp.ActivityUtils.getLoadedDays
import com.hikko.scheduleapp.pages.pageEditActivities.EditActivitiesOfDay
import com.hikko.scheduleapp.R

@Suppress("UNCHECKED_CAST")
class EditActivityAdapter(context: EditActivitiesOfDay, private val resourceLayout: Int, activities: ArrayList<Activity>) :
    ArraySwipeAdapter<Activity?> (context, resourceLayout, activities as ArrayList<Activity?>?) {

    private val mContext: Context
    private val activities: ArrayList<Activity>
    private val nameAutofillHints: MutableList<String?> = ArrayList()
    private val cabinetAutofillHints: MutableList<String?> = ArrayList()
    private val startTimeAutofillHints: MutableList<String?> = ArrayList()
    private val endTimeAutofillHints: MutableList<String?> = ArrayList()

    init {
        mContext = context
        this.activities = activities
        for (dayOfEpoch in getLoadedDays()) {
            for (activity in dayOfEpoch.activitiesList) {
                val activityName = activity.name
                val activityCabinet = activity.cabinet
                val startTime = activity.startTime
                val endTime = activity.endTime

                if (activityName.isNotEmpty() && !nameAutofillHints.contains(activityName)) {
                    nameAutofillHints.add(activityName)
                }
                if (activityCabinet.isNotEmpty() && !cabinetAutofillHints.contains(activityCabinet)) {
                    cabinetAutofillHints.add(activityCabinet)
                }
                if (startTime.isNotEmpty() && !startTimeAutofillHints.contains(startTime)) {
                    startTimeAutofillHints.add(startTime)
                }
                if (endTime.isNotEmpty() && !endTimeAutofillHints.contains(endTime)) {
                    endTimeAutofillHints.add(endTime)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            val vi: LayoutInflater = LayoutInflater.from(mContext)
            view = vi.inflate(resourceLayout, null)!!
        }
        var previousActivity: Activity? = null
        if (position > 0) previousActivity = getItem(position - 1) as Activity

        val activity = getItem(position) as Activity?

        view.findViewById<View>(R.id.SwipeLayout)
            .setOnTouchListener { _: View?, _: MotionEvent? ->
                clearInputFocus(
                    view, mContext
                )
            }
        view.findViewById<View>(R.id.buttonDeleteActivity)
            .setOnTouchListener { _: View?, _: MotionEvent? ->
                deleteActivity(position)
                false
            }

        // Растянуть кнопку по всей высоте родительского элемента
        val viewToStretch = view.findViewById<View>(R.id.buttonDeleteActivity)
        val parentView = viewToStretch.parent as View
        parentView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                parentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val parentHeight = parentView.height
                viewToStretch.layoutParams.height = parentHeight
                viewToStretch.requestLayout()
            }
        })

        val spinner = view.findViewById<Spinner>(R.id.edit_activity_type_spinner)
        if (spinner.adapter == null) {
            val sAdapter = createFromResource(
                view.context,
                R.array.activityType, R.layout.main_activity_day_item_spinner
            )
            sAdapter.setDropDownViewResource(R.layout.main_activity_day_item_spinner_dropdown)
            spinner.adapter = sAdapter
        }
        spinner.setSelection(getSpinnerIndex(spinner, activity!!.type))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position1: Int,
                id: Long
            ) {
                val temp = EditActivitiesOfDay.activitiesOfDayList[position]
                temp.type = spinner.getItemAtPosition(position1) as String
                EditActivitiesOfDay.activitiesOfDayList[position] = temp
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val maxLengthActivityName = view.resources.getInteger(R.integer.max_len_ActivityName)
        val maxLengthActivityCabinet = view.resources.getInteger(R.integer.max_len_ActivityCabinet)
        val inputFiltersActivityName = inputFilters(maxLengthActivityName)
        val inputFiltersActivityCabinet = inputFilters(maxLengthActivityCabinet)
        val activityNameText =
            view.findViewById<AutoCompleteTextView>(R.id.ActivityNameAutoCompleteText)
        val activityNameCabinet =
            view.findViewById<AutoCompleteTextView>(R.id.ActivityCabinetAutoCompleteText)

        // Установка фильтров на input action
        activityNameText.filters = inputFiltersActivityName
        activityNameCabinet.filters = inputFiltersActivityCabinet
        activityNameText.onFocusChangeListener =
            OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                if (hasFocus) {
                    val temp = EditActivitiesOfDay.activitiesOfDayList[position]
                    activityNameText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable) {
                            temp.name = s.toString()
                            EditActivitiesOfDay.activitiesOfDayList[position] = temp
                        }
                    })
                }
            }
        activityNameCabinet.onFocusChangeListener =
            OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                if (hasFocus) {
                    val temp = EditActivitiesOfDay.activitiesOfDayList[position]
                    activityNameCabinet.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable) {
                            temp.cabinet = s.toString()
                            EditActivitiesOfDay.activitiesOfDayList[position] = temp
                        }
                    })
                }
            }


        // Установка текста после загрузки
        activityNameText.setText(activity.name)
        activityNameCabinet.setText(activity.cabinet)

        addPreDrawListener(activityNameText, view)
        addPreDrawListener(activityNameCabinet, view)

        // Автозаполнение текста
        activityNameText.setAdapter(
            ArrayAdapter(
                this.context,
                android.R.layout.simple_dropdown_item_1line, nameAutofillHints
            )
        )
        activityNameCabinet.setAdapter(
            ArrayAdapter(
                this.context,
                android.R.layout.simple_dropdown_item_1line, cabinetAutofillHints
            )
        )
        val inputStartTime = view.findViewById<AutoCompleteTextView>(R.id.input_time_start_of_activity)
        inputStartTime.setText(activity.startTime)
        inputStartTime.setAdapter(
            ArrayAdapter(
                this.context,
                android.R.layout.simple_dropdown_item_1line, startTimeAutofillHints
            )
        )

        val inputEndTime = view.findViewById<AutoCompleteTextView>(R.id.input_time_end_of_activity)
        inputEndTime.setText(activity.endTime)
        inputEndTime.setAdapter(
            ArrayAdapter(
                this.context,
                android.R.layout.simple_dropdown_item_1line, endTimeAutofillHints
            )
        )

        if (inputStartTime.text.isEmpty() && inputEndTime.text.isEmpty() && position > 0) {
            if (previousActivity != null && previousActivity.startTime.isNotEmpty()) {
                if (startTimeAutofillHints.contains(previousActivity.startTime)) {
                    val index = startTimeAutofillHints.indexOf(previousActivity.startTime)
                    if (index + 1 < startTimeAutofillHints.size) {
                        inputStartTime.setText(startTimeAutofillHints[index + 1])
                        inputEndTime.setText(endTimeAutofillHints[index + 1])

                        val temp = EditActivitiesOfDay.activitiesOfDayList[position]
                        temp.startTime = inputStartTime.text.toString()
                        temp.endTime = inputEndTime.text.toString()
                        EditActivitiesOfDay.activitiesOfDayList[position] = temp
                    }
                }
            }
        }

        val timeFilter = arrayOfNulls<InputFilter>(1)
        timeFilter[0] =
            InputFilter { source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int ->
                if (source.isEmpty()) return@InputFilter null

                var result = ""
                result += dest.toString().substring(0, dstart)
                result += source.toString().substring(start, end)
                result += dest.toString().substring(dend, dest.length)

                if (result.length > 5) return@InputFilter ""

                var allowEdit = true
                var c: Char
                if (result.isNotEmpty()) {
                    c = result[0]
                    allowEdit = c in '0'..'2'
                }
                if (result.length > 1) {
                    c = result[1]
                    allowEdit = if (result[0].toString().toInt() > 1) {
                        allowEdit and (c in '0'..'3')
                    } else {
                        allowEdit and (c in '0'..'9')
                    }
                }
                if (result.length > 2) {
                    c = result[2]
                    allowEdit = allowEdit and (c == ':')
                }
                if (result.length > 3) {
                    c = result[3]
                    allowEdit = allowEdit and (c in '0'..'5')
                }
                if (result.length > 4) {
                    c = result[4]
                    allowEdit = allowEdit and (c in '0'..'9')
                }
                if (allowEdit) null else ""
            }
        inputStartTime.filters = timeFilter
        inputEndTime.filters = timeFilter
        val decorationTimeTextWatcher: TextWatcher = object : TextWatcher {
            var isAddChar = true
            override fun afterTextChanged(s: Editable) {
                if (s.length == 2) {
                    if (isAddChar) {
                        s.append(':')
                    } else {
                        s.delete(s.length - 1, s.length)
                    }
                }
                if (s.length == 5) {
                    if (inputStartTime.isFocused) {
                        println(s.toString())
                        println(startTimeAutofillHints.contains(s.toString()))
                        if (startTimeAutofillHints.contains(s.toString())) {
                            val index = startTimeAutofillHints.indexOf(s.toString())
                            if (inputEndTime.text.isEmpty()) {
                                inputEndTime.setText(endTimeAutofillHints[index])

                                val temp = EditActivitiesOfDay.activitiesOfDayList[position]
                                temp.endTime = inputEndTime.text.toString()
                                EditActivitiesOfDay.activitiesOfDayList[position] = temp
                            }
                        }
                        inputEndTime.requestFocus()
                        inputEndTime.setSelection(inputEndTime.text.length)
                    }
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                isAddChar = before < count
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        }
        inputStartTime.addTextChangedListener(decorationTimeTextWatcher)
        inputEndTime.addTextChangedListener(decorationTimeTextWatcher)
        inputStartTime.onFocusChangeListener =
            OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                if (hasFocus) inputStartTime.addTextChangedListener(
                    saveInputTimeInList(
                        "Start",
                        position
                    )
                )
            }
        inputEndTime.onFocusChangeListener =
            OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                if (hasFocus) inputEndTime.addTextChangedListener(
                    saveInputTimeInList(
                        "End",
                        position
                    )
                )
            }
        return view
    }

    private fun getSpinnerIndex(spinner: Spinner, myString: String?): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(myString, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    private fun inputFilters(maxLength: Int): Array<InputFilter> {
        return arrayOf(
            LengthFilter(maxLength),
            InputFilter { source: CharSequence, _: Int, _: Int, _: Spanned?, _: Int, _: Int ->
                if (source.isEmpty()) {
                    return@InputFilter null
                }
                val result = source.toString()
                val char = result[result.length - 1]
                if (char != '\n') null else result.replace("\n", "")
            }
        )
    }

    private fun saveInputTimeInList(key: String, position: Int): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val temp = EditActivitiesOfDay.activitiesOfDayList[position]
                if (key == "Start") {
                    temp.startTime = s.toString()
                } else if (key == "End") {
                    temp.endTime = s.toString()
                }
                EditActivitiesOfDay.activitiesOfDayList[position] = temp
            }
        }
    }

    private fun addPreDrawListener(textView: TextView, view: View) {
        textView.viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
            private var textViewHeight = -1

            override fun onPreDraw(): Boolean {
                if (textViewHeight == -1) {
                    textViewHeight = textView.height
                } else if (textView.height != textViewHeight) {
                    // Текст перенесся на другую строку
                    onTextWrapped(view)
                    textViewHeight = textView.height
                }
                return true
            }
        })
    }

    private fun onTextWrapped(view: View) {
        println("Текст был перенесен на другую строку")
        val viewToStretch = view.findViewById<View>(R.id.buttonDeleteActivity)
        val parentView = viewToStretch.parent as View

        parentView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                parentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val parentHeight = parentView.height
                viewToStretch.layoutParams.height = parentHeight
                println(parentHeight)
                viewToStretch.requestLayout()
            }
        })
        parentView.requestLayout()
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return 0
    }

    // todo
    private fun deleteActivity(pos: Int) {
        EditActivitiesOfDay.deleteActivity(pos)
        activities.removeAt(pos)
        notifyDataSetChanged()
    }
}