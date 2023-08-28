package monopol.rules;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IEvents extends Remote {
    void onGameStart() throws RemoteException;
    void onNextRound() throws RemoteException;
    void onPrisonerRound() throws RemoteException;
    void onDiceRoll() throws RemoteException;
    void onTryMortgage() throws RemoteException;
    void onPurchaseBuilding() throws RemoteException;
    void onSellBuilding() throws RemoteException;
    void onPurchaseCard() throws RemoteException;
    void onArrivedAtLos() throws RemoteException;
    void onArrivedAtAuction() throws RemoteException;
    void onArrivedAtBusPass() throws RemoteException;
    void onArrivedAtBirthday() throws RemoteException;
    void onArrivedAtStreetOrFacility() throws RemoteException;
    void onArrivedAtEventField() throws RemoteException;
    void onArrivedAtCommunityField() throws RemoteException;
    void onArrivedAtFreeParking() throws RemoteException;
    void onArrivedAtGoToPrisonField() throws RemoteException;
    void onArrivedAtTaxField() throws RemoteException;
    void onArrivedAtAdditionalTaxField() throws RemoteException;
    void onPassedLos() throws RemoteException;
    void onOfferTrade() throws RemoteException;
    void onAcceptTrade() throws RemoteException;
    void onGoBankrupt() throws RemoteException;
}
