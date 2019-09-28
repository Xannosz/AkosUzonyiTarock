package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.*;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.*;
import com.facebook.*;
import com.tisza.tarock.R;
import com.tisza.tarock.proto.*;

public class MainActivity extends AppCompatActivity implements GameListAdapter.GameAdapterListener
{
	private static final int DISCONNECT_DELAY_SEC = 40;

	private CallbackManager callbackManager;

	private ConnectionViewModel connectionViewModel;
	private ProgressDialog progressDialog;

	private Handler handler;
	private Runnable disconnectRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		FacebookSdk.sdkInitialize(this.getApplicationContext());
		callbackManager = CallbackManager.Factory.create();

		ResourceMappings.init(this);
		setContentView(R.layout.main);
		connectionViewModel = ViewModelProviders.of(this).get(ConnectionViewModel.class);
		connectionViewModel.getConnectionState().observe(this, this::connectionStateChanged);
		progressDialog = new ProgressDialog(this);
		handler = new Handler();
		disconnectRunnable = connectionViewModel::disconnect;

		LoginFragment loginFragment = new LoginFragment();
		getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, loginFragment, "login")
				.commit();

		if (getIntent().hasExtra("game_id"))
			connectionViewModel.login();
	}

	private void connectionStateChanged(ConnectionViewModel.ConnectionState connectionState)
	{
		switch (connectionState)
		{
			case DISCONNECTED:
			case CONNECTED:
				popBackToLoginScreen();
				break;
			case CONNECTING:
				progressDialog.setMessage(getResources().getString(R.string.connecting));
				progressDialog.setOnDismissListener(x -> connectionViewModel.disconnect());
				progressDialog.show();
				break;
			case LOGGING_IN:
				progressDialog.setMessage(getResources().getString(R.string.logging_in));
				progressDialog.show();
				break;
			case LOGGED_IN:
				onSuccessfulLogin();
				break;
			case DISCONNECTED_OUTDATED:
				popBackToLoginScreen();
				requireUpdate();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	private void onSuccessfulLogin()
	{
		popBackToLoginScreen();

		GameListFragment gameListFragment = new GameListFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, gameListFragment, "gamelist")
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();

		String gameID = getIntent().getStringExtra("game_id");
		if (gameID != null)
		{
			getIntent().removeExtra("game_id");
			joinGame(Integer.parseInt(gameID));
		}
	}

	public void createNewGame()
	{
		CreateGameFragment createGameFragment = new CreateGameFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, createGameFragment, "create_game")
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void joinGame(int gameID)
	{
		GameFragment gameFragment = new GameFragment();

		Bundle args = new Bundle();
		args.putInt("gameID", gameID);
		gameFragment.setArguments(args);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, gameFragment, "game")
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void deleteGame(int gameID)
	{
		connectionViewModel.sendMessage(MainProto.Message.newBuilder().setDeleteGame(MainProto.DeleteGame.newBuilder()
				.setGameId(gameID))
				.build());
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		handler.removeCallbacks(disconnectRunnable);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		handler.postDelayed(disconnectRunnable,  DISCONNECT_DELAY_SEC * 1000);
	}

	private void popBackToLoginScreen()
	{
		progressDialog.setOnDismissListener(null);
		if (progressDialog.isShowing())
			progressDialog.dismiss();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, new LoginFragment(), "login")
				.commit();
	}

	private void requireUpdate()
	{
		new AlertDialog.Builder(this)
				.setTitle(R.string.update_available)
				.setMessage(R.string.update_please)
				.setPositiveButton(R.string.update_accept, (dialog, which) ->
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()))))
				.setNegativeButton(R.string.update_deny, (dialog, which) ->
						finish())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}
}
