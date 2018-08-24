# ListDelete
swipe list to delete, with lock actions

1. Fetches json data through endpoint https://restcountries.eu/rest/v2/all
2. Display json data as listview with following elements parsed from json
  a. “name”  ->  this is Country Name
  b  “currencies” -> ”name" -> this is currency name, if more than 1 currency is present, first currency name is to be displayed
  c. “languages” -> “name”  -> this is language name, if more than 1 language is present first language is to be used
3. On the displayed data, add a custom "Swipe to delete" with the following characteristics:
  • Row background color -  white
  • Swipe background color - purple
  • Delete icon -  (if you cannot see it here - it should be attached -it is a bomb)
  • It should have an anchor point for where the swipe just shows the delete icon.
  • On a fast swipe past anchor point the row should delete.
  • On a slow swipe past anchor point, swiped area should swipe back to anchor point showing the delete icon
  • On a slow swipe till anchor point, the swipe should get cancelled.
  • Clicking/Swiping any other row should cancel the current swipe.

-----------------------------------------------------------------------

![git demo](https://github.com/pebertli/ListDelete/blob/master/ListDelete.gif)

- API 19 to API 28
- The Json parser only look to name, currency and language
- Works perfectly with differents screen sizes and density
- Using RecycleView
- Each row has his own movement swipe Listener and delete click listener, however there is an proxy to handle this two listeners
- The Adapter take care of recycled layouts of ViewHolder in onBindViewHolder
- You can control swipe speed and anchor point in adjusting the constants SWIPE_SPEED, ANCHOR_POINT of SwipeRowHelper, but of course that it can be made customized by user
- SwipeRowHelper receives xDPFactor to ensure that speed and swipe anchor point works equally on diferent screen sizes and densities
- The control of swipe was developed using onTouch to make sure a fine control.
- VelocityTracker calculate the swipe velocity and direction
- recyclerView.requestDisallowInterceptTouchEvent(true);is called during touch MOVE to make sure that the swipe is not canceled
- The alpha is changed during the swipe. I have choosed to change the alpha only if the swipe past the archor_point, however if you wants to change the alpha as soon the swipe starts, uncomment the line ((View)v.getParent()).setAlpha(1 - Math.abs(newPos/v.getWidth()));
- The currentRow is used to make sure that only one row can be marked/swiped at on time
- Touch UP check if the swipe past the swipe and his velocity to decided if row should be deleted directly, marked (show the delete button) or return to initial state
- Delete button has a OnClickListener to dispatch the delete action
