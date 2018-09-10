package app.autowatering.main

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import app.autowatering.R
import app.autowatering.databinding.ActivityMainBinding
import app.autowatering.obtainViewModel
import app.autowatering.showToast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_ENABLE_BT = 334

        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val viewModel: MainViewModel by lazy {
        obtainViewModel(MainViewModel::class.java)
    }

    private val adapter = WateringScriptsArrayAdapter()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        configureView()

        with(viewModel) {
            scriptsList.observe(this@MainActivity, Observer {
                adapter.update(it!!)
            })

            toastPub.observe(this@MainActivity, Observer {
                showToast(it!!)
            })
        }

        binding.viewModel = viewModel

        viewModel.start()
    }

    private fun configureView() {
        syncButton.setOnClickListener({
            viewModel.onRefreshClick()
        })

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()

        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.listener = viewModel
//        WateringScriptsArrayAdapter().apply {
//            val items = (0 until 5).mapTo(arrayListOf()) {
//                WateringScriptViewPresentation().apply {
//                    scriptIdText = "#$it"
//                    startInText = "Start in: $it"
//                    durationText = "Duration: $it"
//                    intervalText = "Interval: $it"
//                }
//            }
//
//            update(items)
//        }

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View,
                                        parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)

                if (parent.getChildAdapterPosition(view) == state.itemCount - 1) {
                    outRect.bottom = 200
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
