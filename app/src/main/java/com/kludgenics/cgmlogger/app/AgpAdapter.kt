package com.kludgenics.cgmlogger.app

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.kludgenics.cgmlogger.app.view.ChartView
import io.realm.Realm
import org.jetbrains.anko.*
import com.kludgenics.cgmlogger.app.R
/**
 * Created by matthiasgranberry on 5/31/15.
 */
public class AgpAdapter(private val realm: Realm): RecyclerView.Adapter<AgpAdapter.ViewHolder>() {
    class ViewHolder(public val agpView: View): RecyclerView.ViewHolder(agpView) {
    }

    override fun onBindViewHolder(p0: ViewHolder?, p1: Int) {

        throw UnsupportedOperationException()
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val cardView = CardView(viewGroup.getContext())
        cardView.frameLayout {
            imageView(R.drawable.ic_drawer) {
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            textView {

            }
        }
        cardView.contentDescription = "Graph of blood glucose"

        return ViewHolder(cardView)
    }


    override fun getItemCount(): Int {
        return 0
    }
}
