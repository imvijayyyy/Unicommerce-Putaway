package com.prozo.putaway

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : Activity() {
 private val pickCode = 42
 private var values = mutableListOf<String>()
 private lateinit var status: TextView
 private lateinit var delay: EditText
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val box = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,30,28,24) }
  box.addView(TextView(this).apply { text="Unicommerce Putaway Assistant"; textSize=23f })
  box.addView(TextView(this).apply { text="Excel column A: A2 = shelf code; A3 onward = barcode URLs." })
  box.addView(Button(this).apply { text="1. Select Excel / CSV"; setOnClickListener {
   startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type="*/*"; putExtra(Intent.EXTRA_MIME_TYPES,arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","text/csv")) },pickCode)
  }})
  delay=EditText(this).apply { hint="Delay in milliseconds"; setText("2000"); inputType=2 }; box.addView(delay)
  box.addView(Button(this).apply { text="2. Enable Accessibility"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } })
  box.addView(Button(this).apply { text="3. Start (then return to Unicommerce)"; setOnClickListener {
   if(values.size<2) status.text="Select a file with shelf code in A2 and barcode(s) from A3."
   else { PutawayAccessibilityService.begin(values,delay.text.toString().toLongOrNull()?.coerceIn(1000,10000)?:2000L); status.text="Run requested. Keep Unicommerce open with its scan field focused." }
  }})
  box.addView(Button(this).apply { text="Pause / Resume"; setOnClickListener { PutawayAccessibilityService.togglePause(); status.text="Pause/resume requested." } })
  box.addView(Button(this).apply { text="Stop"; setOnClickListener { PutawayAccessibilityService.stopRun(); status.text="Stopped." } })
  status=TextView(this).apply { text="No file selected."; textSize=16f }; box.addView(status)
  box.addView(TextView(this).apply { text="The app will not tap Close Putaway. Test with one barcode first." })
  setContentView(ScrollView(this).apply { addView(box) })
 }
 @Deprecated("Deprecated")
 override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
  super.onActivityResult(requestCode,resultCode,data)
  if(requestCode!=pickCode || resultCode!=RESULT_OK) return
  val uri=data?.data?:return
  try {
   contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
   values=readA(uri)
   status.text="Loaded ${values.size} values. First value (shelf code): ${values.firstOrNull()?: "missing"}"
  } catch(e:Exception) { status.text="File read error: ${e.message}" }
 }
 private fun readA(uri:Uri):MutableList<String> {
  val isCsv=uri.lastPathSegment?.lowercase()?.endsWith(".csv")==true || contentResolver.getType(uri)?.contains("csv")==true
  if(isCsv) return BufferedReader(InputStreamReader(contentResolver.openInputStream(uri)!!)).useLines { seq ->
   seq.map { it.substringBefore(",").trim().trim('"') }.filter { it.isNotEmpty() }.toMutableList()
  }
  contentResolver.openInputStream(uri).use { stream ->
   val wb=WorkbookFactory.create(stream); val sheet=wb.getSheetAt(0); val out=mutableListOf<String>()
   for(r in 1..sheet.lastRowNum) { val v=sheet.getRow(r)?.getCell(0)?.toString()?.trim().orEmpty(); if(v.isNotEmpty()) out.add(v) }
   wb.close(); return out
  }
 }
}
