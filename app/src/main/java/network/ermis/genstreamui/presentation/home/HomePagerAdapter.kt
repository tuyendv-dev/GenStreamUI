package network.ermis.genstreamui.presentation.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import network.ermis.genstreamui.presentation.home.discovery.DiscoveryFragment
import network.ermis.genstreamui.presentation.home.find.FindGameFragment
import network.ermis.genstreamui.presentation.home.mine.MineFragment

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MineFragment()
            1 -> DiscoveryFragment()
            2 -> FindGameFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}