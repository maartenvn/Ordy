package com.ordy.app.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.nhaarman.mockitokotlin2.whenever
import com.ordy.app.R
import com.ordy.app.api.Repository
import com.ordy.app.api.models.User
import com.ordy.app.api.util.Query
import com.ordy.app.api.util.QueryStatus
import com.ordy.app.api.wrappers.GroupInviteUserWrapper
import com.ordy.app.ui.groups.invite.InviteMemberActivity
import com.ordy.app.ui.groups.invite.InviteMemberViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
class GroupsInvitesTest : KoinTest {

    /**
     * Java faker for faking data.
     */
    private val faker = Faker()

    /**
     * Mocked android activity context.
     */
    private lateinit var mockContext: Context

    /**
     * Viewmodel that has been created using Koin injection.
     */
    private val mockInviteMemberViewModel: InviteMemberViewModel by inject()

    /**
     * Mock provider for Koin testing.
     */
    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Before
    fun setup() {

        // Initialize mocks
        mockContext = InstrumentationRegistry.getInstrumentation().targetContext

        declareMock<Repository>()
        declareMock<InviteMemberViewModel>()
    }

    /**
     * Check if the button of an uninvited user is set to "INVITE"
     */
    @Test
    fun displayInviteButton() {
        val inviteWrappers = listOf(
            GroupInviteUserWrapper(
                user = User(
                    id = 1,
                    username = "member one",
                    email = faker.name().name()
                ),
                invited = false
            )
        )

        val groupInviteQuery: Query<List<GroupInviteUserWrapper>> = Query()
        groupInviteQuery.status = QueryStatus.SUCCESS
        groupInviteQuery.data = inviteWrappers

        val inviteableUsersMLD = MutableLiveData(groupInviteQuery)

        // Mock the viewmodel
        whenever(mockInviteMemberViewModel.getInviteableUsersMLD()).thenReturn(inviteableUsersMLD)
        whenever(mockInviteMemberViewModel.getSearchValueData()).thenReturn(MutableLiveData("member"))

        // Create intent to open activity
        val intent = Intent(mockContext, InviteMemberActivity::class.java)

        // Launch the activity
        ActivityScenario.launch<InviteMemberActivity>(intent)

        // Check if the invite member button shows "INVITE"
        onView(withId(R.id.member_invite))
            .check(matches(withText(R.string.invite_button)))
    }

    /**
     * Check if the button of an invited user is set to "INVITED!"
     */
    @Test
    fun displayInvitedButton() {
        val inviteWrappers = listOf(
            GroupInviteUserWrapper(
                user = User(
                    id = 1,
                    username = "member one",
                    email = faker.name().name()
                ),
                invited = true
            )
        )

        val groupInviteQuery: Query<List<GroupInviteUserWrapper>> = Query()
        groupInviteQuery.status = QueryStatus.SUCCESS
        groupInviteQuery.data = inviteWrappers

        val inviteableUsersMLD = MutableLiveData(groupInviteQuery)

        // Mock the viewmodel
        whenever(mockInviteMemberViewModel.markUserAsInvited(1)).then { }
        whenever(mockInviteMemberViewModel.isUserInvited(1)).thenReturn(true)
        whenever(mockInviteMemberViewModel.getInviteableUsersMLD()).thenReturn(inviteableUsersMLD)
        whenever(mockInviteMemberViewModel.getSearchValueData()).thenReturn(MutableLiveData("member"))

        // Create intent to open activity
        val intent = Intent(mockContext, InviteMemberActivity::class.java)

        // Launch the activity
        ActivityScenario.launch<InviteMemberActivity>(intent)

        // Check if the invite member button shows "INVITE"
        onView(withId(R.id.member_invite))
            .check(matches(withText(R.string.invited_button)))
    }
}