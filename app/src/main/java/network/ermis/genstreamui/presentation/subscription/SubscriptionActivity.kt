package network.ermis.genstreamui.presentation.subscription

import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import network.ermis.genstreamui.R
import network.ermis.genstreamui.presentation.addScaleClickEffect
import network.ermis.genstreamui.databinding.ActivitySubscriptionBinding

import network.ermis.genstreamui.presentation.widget.setupStatusIcons

@AndroidEntryPoint
class SubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var planAdapter: SubscriptionPlanAdapter
    private lateinit var featureAdapter: PlanFeatureAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Setup status icons
        setupStatusIcons(
            binding = binding.statusIcons,
            showSearch = false,
            showGamepad = false
        )

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAction.addScaleClickEffect()

        val featuresExperience = listOf(
            PlanFeature(R.drawable.ic_sub_gpu, "GPU: RTX 3060Ti 8GB RAM"),
            PlanFeature(R.drawable.ic_sub_time, "Limited to 3 hours"),
            PlanFeature(R.drawable.ic_sub_cpu, "CPU: Xeon 8171M 6 Cores"),
            PlanFeature(R.drawable.ic_sub_limit, "Time limit 3 hours/session"),
            PlanFeature(R.drawable.ic_sub_ram, "RAM: 16GB DDR4"),
            PlanFeature(R.drawable.ic_sub_nosupport, "No idle support"),
            PlanFeature(R.drawable.ic_sub_store, "Data is not saved")
        )

        val featuresStandard = listOf(
            PlanFeature(R.drawable.ic_sub_gpu, "GPU: RTX 3060Ti 8GB RAM"),
            PlanFeature(R.drawable.ic_sub_store, "Free 10GB usage data"),
            PlanFeature(R.drawable.ic_sub_cpu, "CPU: Xeon 8171M 6 Cores"),
            PlanFeature(R.drawable.ic_sub_time, "Limited to 120 hours"),
            PlanFeature(R.drawable.ic_sub_ram, "RAM: 16GB DDR4"),
            PlanFeature(R.drawable.ic_sub_limit, "Time limit 3 hours/session"),
            PlanFeature(R.drawable.ic_sub_store, "Free 200GB"),
            PlanFeature(R.drawable.ic_sub_check, "Uptime guarantee"),
            PlanFeature(R.drawable.ic_sub_store, "After 200GB, $0.4/10GB"),
            PlanFeature(R.drawable.ic_sub_nosupport, "No idle support")
        )

        val featuresPerformance = listOf(
            PlanFeature(R.drawable.ic_sub_gpu, "GPU: RTX 5060Ti 16GB RAM"),
            PlanFeature(R.drawable.ic_sub_time, "Limited to 360 hours"),
            PlanFeature(R.drawable.ic_sub_cpu, "CPU: AMD EPYC Milan-X 14 cores"),
            PlanFeature(R.drawable.ic_sub_limit, "No limit on hours per session"),
            PlanFeature(R.drawable.ic_sub_ram, "RAM: 24GB DDR4"),
            PlanFeature(R.drawable.ic_sub_check, "Uptime guarantee"),
            PlanFeature(R.drawable.ic_sub_store, "Free 400GB"),
            PlanFeature(R.drawable.ic_sub_check, "No queue"),
            PlanFeature(R.drawable.ic_sub_store, "After 400GB, $0.4/10GB"),
            PlanFeature(R.drawable.ic_sub_check, "Service priority"),
            PlanFeature(R.drawable.ic_sub_store, "Free 20GB usage data"),
            PlanFeature(R.drawable.ic_sub_nosupport, "No idle support")
        )

        val plans = listOf(
            SubscriptionPlan("exp", "Experience", "5 hour trial plan with unlimited days.\nCan only be upgraded to other plans", "$1.12", "/3h", false, "Upgrade plan", featuresExperience, R.drawable.ic_sub_1),
            SubscriptionPlan("std", "Standard", "Balanced choice for work and gaming at medium-high settings", "$12", "/month", true, "Renew plan", featuresStandard, R.drawable.ic_sub_2),
            SubscriptionPlan("perf", "Performance", "Maximum power for the games at highest performance", "$24", "/month", false, "Upgrade plan", featuresPerformance, R.drawable.ic_sub_3)
        )

        featureAdapter = PlanFeatureAdapter(emptyList())
        binding.rvFeatures.adapter = featureAdapter

        planAdapter = SubscriptionPlanAdapter(plans) { plan ->
            updatePlanDetails(plan)
            binding.scrollView.smoothScrollTo(0, 0)
        }
        binding.rvPlans.layoutManager = LinearLayoutManager(this)
        binding.rvPlans.adapter = planAdapter

        // Initial selection
        val initialPlan = plans[planAdapter.selectedPosition]
        updatePlanDetails(initialPlan)
    }

    private fun updatePlanDetails(plan: SubscriptionPlan) {
        binding.tvDetailTitle.text = plan.name
        binding.tvDetailSubtitle.text = plan.subtitle
        binding.tvDetailPrice.text = plan.price
        binding.tvDetailPriceUnit.text = plan.priceUnit
        binding.btnAction.text = plan.buttonText

        if (plan.isCurrentPlan && plan.name == "Standard") {
            binding.tvDetailTrial.visibility = View.VISIBLE
            binding.tvDetailTrial.text = "Due date: Jan 23, 2027"
        } else {
            binding.tvDetailTrial.visibility = View.GONE
        }

        featureAdapter.updateData(plan.features)
    }
}
