package com.prozo.putaway

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class PutawayAccessibilityService : AccessibilityService() {
 companion object {
  @Volatile private var service:PutawayAccessibilityService?=null
  @Volatile private var queue:List<String> = emptyList()
  @Volatile private var delay=2000L
  @Volatile private var running=false
  @Volatile private var paused=false
  fun begin(rows:List<String>,wait:Long) { queue=rows; delay=wait; paused=false; running=true; service?.startNewRun() }
  fun togglePause() { paused=!paused; if(!paused) service?.schedule(0) }
  fun stopRun() { running=false; paused=false }
 }
 private val handler=Handler(Looper.getMainLooper())
 private var position=0
 override fun onServiceConnected() { service=this }
 override fun onAccessibilityEvent(event:AccessibilityEvent?) {}
 override fun onInterrupt() { stopRun() }
 private fun startNewRun() { position=0; schedule(1200) }
 fun schedule(wait:Long) {
  handler.postDelayed({
   if(!running || paused) return@postDelayed
   if(position>=queue.size) { running=false; return@postDelayed }
   val node=findEditable(rootInActiveWindow)
   if(node==null) { running=false; return@postDelayed }
   val text=queue[position]
   val args=Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text) }
   val changed=node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args)
   val entered=node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id)
   if(!changed || !entered) { running=false; return@postDelayed }
   position++
   schedule(delay)
  },wait)
 }
 private fun findEditable(node:AccessibilityNodeInfo?):AccessibilityNodeInfo? {
  if(node==null) return null
  if(node.isEditable && node.isEnabled && node.isVisibleToUser) return node
  for(i in 0 until node.childCount) { val found=findEditable(node.getChild(i)); if(found!=null) return found }
  return null
 }
}
