package com.pebertli.listdelete.helper;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;
import android.widget.Toast;

import com.pebertli.listdelete.R;
import com.pebertli.listdelete.adapters.CountriesAdapter.CountryViewHolder;
import com.pebertli.listdelete.models.Country;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SwipeRowHelper
{
    private static final int SWIPE_DURATION = 200;//in milliseconds
    private static final int SWIPE_SPEED = -1000; //... pixels per second to left //higher absolute values demand a faster swipe
    private static final int SWIPE_SPEED_UNIT = 1000; //pixels per second
    private static final int ANCHOR_POINT = 70;//in dp

    private int swipeSlope = 1;
    private VelocityTracker velocityTracker;
    private float xDPFactor;

    private View currentRow = null;
    private boolean touched = false; //forbidden multitouch

    public RecyclerView recyclerView;
    public List<Country> rows;



    public SwipeRowHelper(RecyclerView recyclerView, List<Country> rows, float xDPFactor)
    {
        this.recyclerView = recyclerView;
        this.rows = rows;
        this.xDPFactor = xDPFactor;
        this.swipeSlope = ViewConfiguration.get(recyclerView.getContext()).getScaledTouchSlop();
    }

   public SwipeTouchListener getNewSwipeTouchListener(CountryViewHolder holder)
   {
       return new SwipeTouchListener(holder);
   }

    public SwipeButtonClickListener getNewSwipeButtonClickListener(CountryViewHolder holder)
    {
        return new SwipeButtonClickListener(holder);
    }

    private void animateAndRemoveItem(View v, final CountryViewHolder holder)
    {
        recyclerView.setEnabled(false);
        ((View)v.getParent()).animate().setDuration(SWIPE_DURATION).alpha(0);
        v.animate().setDuration(SWIPE_DURATION).translationX(-v.getWidth()).withEndAction(new Runnable() {
            @Override
            public void run()
            {
                int p = holder.getAdapterPosition();
                rows.remove(p);
                recyclerView.getAdapter().notifyItemRemoved(p);
                recyclerView.setEnabled(true);
            }
        });
    }

    private void animate(final View v, long duration, final float pos, final float alpha)
    {
        recyclerView.setEnabled(false);
        ((View)v.getParent()).animate().setDuration(duration).alpha(alpha);
        v.animate().setDuration(duration).translationX(pos).withEndAction(new Runnable() {
            @Override
            public void run()
            {
                v.setAlpha(alpha);
                v.setTranslationX(pos);
                recyclerView.setEnabled(true);
            }
        });
    }



    //class
    public class SwipeButtonClickListener implements View.OnClickListener
    {
        private CountryViewHolder holder;

        public SwipeButtonClickListener(CountryViewHolder holder)
        {
            this.holder = holder;
        }

        @Override
        public void onClick(View view)
        {
            if(currentRow != null)
            {
                currentRow = null;
                animateAndRemoveItem(holder.itemView.findViewById(R.id.foregroundLayout), holder);
            }
        }
    }


    //class
    public class SwipeTouchListener implements View.OnTouchListener
    {
        private CountryViewHolder holder;
        private boolean moving = false; //is moving/swiping
        private boolean remove = false; //mark to be removed
        private float firstPressedX; //first down to calculate slope
        private float lastX; //saved every iteraction to get the diff



        public SwipeTouchListener(CountryViewHolder holder)
        {
            this.holder = holder;
        }

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            event.offsetLocation(v.getTranslationX(),0); //fix the movement for calculation of velocity

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(touched)
                        return false;
                    else
                        touched = true;
                    lastX = event.getRawX();
                    firstPressedX = event.getRawX();
                    if(velocityTracker == null)
                        velocityTracker = VelocityTracker.obtain();
                    else
                        velocityTracker.clear();

                    velocityTracker.addMovement(event);
                    //cancel the last swiped row if touch on another row
                    if(currentRow != null && currentRow != v)
                    {
                        moving = false;
                        animate(currentRow, SWIPE_DURATION, 0, 1);
                        currentRow = null;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    //cancel the last swiped row if scroll or cancel the movement
                    moving = false;
                    touched = false;
                    animate(v, SWIPE_DURATION, 0, 1);
                    currentRow = null;
                    velocityTracker.clear();
                    break;
                case MotionEvent.ACTION_MOVE:
                {
                    float delta = event.getRawX() - lastX; //how much the touch moved and in which direction
                    lastX = event.getRawX();//save for the next iteraction
                    float deltaAbs = Math.abs(delta);//how much the touch moved
                    float currentSlope = Math.abs(event.getRawX() - firstPressedX);//how much the touch moved since the first touch
                    if (!moving)
                    {
                        if (currentSlope >= swipeSlope )//tolerance for move on Y
                        {
                            moving = true;
                            velocityTracker.clear(); //reset tracker
                            recyclerView.requestDisallowInterceptTouchEvent(true); //forbidden overall touch (and scroll)
                        }
                    }
                    float newPos = v.getX()+delta; //current position plus the touch movement
                    if (moving && newPos<=0) //for while, just the right swipe
                    {

                        v.setX(newPos); //swipe the row according to touch position difference

                        float multAlphaFactor =  (ANCHOR_POINT*xDPFactor/v.getWidth());
                        float deridedAlpha = 1 - (Math.abs(newPos/v.getWidth())*(1+multAlphaFactor))+ multAlphaFactor;
                        ((View)v.getParent()).setAlpha(deridedAlpha);//alpha starts after the anchor_point or...
                        //((View)v.getParent()).setAlpha(1 - Math.abs(newPos/v.getWidth()));//alpha starts immediately
                    }
                    else if(moving && newPos > 0)//so make sure that will not pass to right layout
                    {
                        ((View)v.getParent()).setAlpha(1);
                        v.setTranslationX(0);
                    }
                    velocityTracker.addMovement(event);//update the velocity

                }
                break;
                case MotionEvent.ACTION_UP:
                {
                    touched = false;
                    if (moving)//is swiping
                    {
                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - lastX;
                        float backToThis = 0;

                        if (Math.abs(v.getX()) > ANCHOR_POINT*xDPFactor) //past the archor point
                        {
                            velocityTracker.computeCurrentVelocity(SWIPE_SPEED_UNIT);
                            if( velocityTracker.getXVelocity() > SWIPE_SPEED*xDPFactor) //not enough to delete directly
                            {
                                backToThis = -ANCHOR_POINT*xDPFactor;
                                currentRow = v;
                            }
                            else//pass the anchor point with enough speed to be deleted
                            {
                                remove = true;
                            }
                        }
                        else //didn't pass the archor point
                        {
                            backToThis = 0;
                            currentRow = null;
                        }
                        moving = false;
                        if(!remove) //don't remove directly, so animate to anchor or to initial state
                            animate(v, SWIPE_DURATION, backToThis, 1);
                        else//marked to be removed, so remove it
                        {
                            animateAndRemoveItem(v, holder);
                            currentRow = null;
                        }
                        remove = false;
                    }
                    else //just a simple click to simulate the row click and navigation to another fragment/activity
                        Toast.makeText(v.getContext(), "Opening details about " + ((TextView)v.findViewById(R.id.name)).getText(), Toast.LENGTH_LONG).show();
                }
                break;
                default:
                    return false;
            }
            return true;
        }

    }


}
