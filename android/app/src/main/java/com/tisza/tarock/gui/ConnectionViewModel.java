package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.net.*;
import androidx.lifecycle.*;
import com.facebook.*;
import com.google.android.gms.auth.api.signin.*;
import com.tisza.tarock.api.*;
import com.tisza.tarock.api.model.*;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.*;

import java.util.*;
import java.util.concurrent.*;

public class ConnectionViewModel extends AndroidViewModel
{
	private MutableLiveData<List<GameSession>> games = new MutableLiveData<>(null);
	private ConnectivityManager connectivityManager;

	private MutableLiveData<ConnectionState> connectionState = new MutableLiveData<>(ConnectionState.CONNECTED);
	private MutableLiveData<ErrorState> errorState = new MutableLiveData<>(null);
	private MutableLiveData<User> user = new MutableLiveData<>(null);

	private APIInterface apiInterface;

	public ConnectionViewModel(Application application)
	{
		super(application);

		apiInterface = APIClient.getClient().create(APIInterface.class);
		connectivityManager = (ConnectivityManager)application.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public LiveData<ConnectionState> getConnectionState()
	{
		return connectionState;
	}

	public LiveData<ErrorState> getErrorState()
	{
		return errorState;
	}

	private void error(ErrorState error)
	{
		errorState.setValue(error);
		errorState.setValue(null);
	}

	public LiveData<List<GameSession>> getGameSessions()
	{
		return games;
	}

	public LiveData<GameSession> getGameSessionByID(int gameSessionId)
	{
		MediatorLiveData<GameSession> mediatorLiveData = new MediatorLiveData<>();
		mediatorLiveData.addSource(games, gameSessionList ->
		{
			GameSession gameSession = null;
			for (GameSession g : gameSessionList)
				if (g.getId() == gameSessionId)
					gameSession = g;

			mediatorLiveData.setValue(gameSession);
		});

		return mediatorLiveData;
	}

	public LiveData<User> getLoggedInUser()
	{
		return user;
	}

	public void login()
	{
		Observable.interval(0, 2, TimeUnit.SECONDS)
				.flatMap(i -> apiInterface.getGameSessions())
				.subscribe(gameSessions -> games.postValue(gameSessions));

		switch (connectionState.getValue())
		{
			case DISCONNECTED:
				/*NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())
					error(ErrorState.NO_NETWORK);
				else
					new ConnectAsyncTask().execute();*/
				break;
			case CONNECTING:
			case LOGGING_IN:
				break;
			case LOGGED_IN:
				connectionState.setValue(ConnectionState.LOGGED_IN);
				break;
			case CONNECTED:
				connectionState.setValue(ConnectionState.LOGGING_IN);
				AccessToken facebookToken = AccessToken.getCurrentAccessToken();
				GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(getApplication());
				LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
				if (facebookToken != null)
				{
					loginRequestDTO.provider = "facebook";
					loginRequestDTO.token = facebookToken.getToken();
				}
				if (googleAccount != null)
				{
					loginRequestDTO.provider = "google";
					loginRequestDTO.token = googleAccount.getIdToken();
				}

				System.out.println("login");
				apiInterface.login(loginRequestDTO).observeOn(AndroidSchedulers.mainThread()).subscribe(loginResponseDTO ->
				{
					user.setValue(loginResponseDTO.user);
					AuthInterceptor.authToken = loginResponseDTO.token;
					System.out.println("authtoken------------------------ " + loginResponseDTO.token);
					connectionState.setValue(ConnectionState.LOGGED_IN);
					//TODO: remove observer
				});
				break;
		}
	}

	public APIInterface getApiInterface()
	{
		return apiInterface;
	}

	public void logout()
	{
		user.setValue(null);
		AuthInterceptor.authToken = null;
	}

	public static enum ConnectionState
	{
		DISCONNECTED, CONNECTING, CONNECTED, LOGGING_IN, LOGGED_IN;
	}

	public static enum ErrorState
	{
		OUTDATED, NO_NETWORK, SERVER_DOWN, SERVER_ERROR, LOGIN_UNSUCCESSFUL;
	}
}
