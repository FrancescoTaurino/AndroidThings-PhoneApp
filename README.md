# Android Things: un primo progetto

Il progetto descritto in questo documento è stato realizzato con il semplice scopo di esplorare e prendere confidenza con il mondo di Android Things.

[Android Things](https://developer.android.com/things/) offre una suite di nuove API, le quali, assieme ai tool Android e le API già esistenti, permettono di interagire con le varie periferiche presenti sul dispositivo, ad esempio: periferiche di I/O, sensori e/o display. Il suo utilizzo permette lo sviluppo di dispositivi smart ed interconnessi, applicabili in qualsiasi settore industriale.

Il [Raspberry Pi](https://www.raspberrypi.org/products/raspberry-pi-3-model-b-plus/) ed il [Rainbow HAT](https://shop.pimoroni.com/products/rainbow-hat-for-android-things) rappresentano un’ottima soluzione per entrare in confidenza con Android Things, dato che quest’ultimo è installabile sul Raspberry Pi, aggiungendo periferiche di I/O, sensori di pressione e temperatura, tasti capacitativi, led multicolore, display e speaker. L’accesso a queste periferiche è possibile utilizzando i seguenti [driver](https://github.com/androidthings/contrib-drivers/tree/master/rainbowhat).

Si noti che il README della pagina Github dei driver riporta:
>these drivers are not production-ready. They are offered as sample implementations of Android Things user space drivers for common peripherals as part of the Developer Preview release. There is no guarantee of correctness, completeness or robustness.

In ogni caso, per avere accesso alle API di Android Things è necessario aggiungere al file `build.gradle` (app-level), la dipendenza: `compileOnly 'com.google.android.things:androidthings:+'`.

## Struttura e caratteristiche del Rainbow HAT
Il Rainbow HAT ha la seguente struttura:
<p align="center">
  <img src="https://francescotaurino.github.io/MediaRepo/AndroidThingsFirstProject/rainbowHAT.jpg" width="500"></p>

e presenta le seguenti caratteristiche:
- Seven APA102 multicolour LEDs
- Four 14-segment alphanumeric displays (green LEDs)
- HT16K33 display driver chip
- Three capacitive touch buttons
- Atmel QT1070 capacitive touch driver chip
- Blue, green and red LEDs
- BMP280 temperature and pressure sensor
- Piezo buzzer
- Breakout pins for servo, I2C, SPI, and UART (all 3v3)
- [Rainbow HAT pinout](https://pinout.xyz/pinout/rainbow_hat)
- Compatible with Raspberry Pi 3B+, 3, 2, B+, A+, Zero, and Zero W
- [Python library](https://github.com/pimoroni/rainbow-hat).

In particolare, per questo progetto, saranno utilizzati i tre tasti capacitativi (A, B e C), i corrispondenti led (blu, verde e rosso) e il display alfanumerico.

## Descrizione generale del progetto
Il progetto realizza una sorta di centralino telefonico (rappresentato dal Raspberry Pi + Rainbow HAT), al quale è possibile lasciare fino a tre messaggi. Ogni volta che il centralino riceve un messaggio accende uno dei tre led; premendo il corrispondente tasto, il messaggio viene visualizzato sul display ed il led si spegne. Infine, i messaggi sono generati da una seconda applicazione installabile su smartphone/tablet Android.

Quindi, il progetto è costituito da due applicazioni:
- `PhoneApp`: sviluppata utilizzando Android classico ed incaricata di generare e inviare i messaggi;
- `RaspApp`: sviluppata utilizzando Android Things ed incaricata di ricevere e visualizzare i messaggi.

## Applicazione per Android: `PhoneApp`
`PhoneApp` è una semplice applicazione per Android, la quale è costituitra da un'unica `Activity` composta dai seguenti widget:
1. `EditText`: nel quale è possibile inserire il messaggio (sono accettati solo caratteri alfanumerici e lo spazio);
2. `Button`: per inviare il messaggio digitato;
3. `RecyclerView`: lista per mostrare all'utente i messaggi inviati.

Ogni item della `RecyclerView` mostra:
1. un ID (in alto a sinistra) che identifica univocamente il messaggio (il metodo di generazione è spiegato in seguito);
2.  il testo del messaggio (al centro);
3. un tick per indicare all'utente se il messaggio è stato visualizzato o meno. In caso negativo il tick ha colore nero, in caso positivo ha colore verde (in basso a destra).

Come spiegato in precedenza, il centralino è capace di accogliere solo tre messaggi. Per questo motivo, non è possibile spedire un nuovo messaggio se sono presenti tre messaggi non letti.

La GIF che segue mostra il comportamento di `PhoneApp`.  Si noti che dopo l'invio dei primi tre messaggi  (GOOFY, PLUTO e MICKEY), il quarto messaggio (DONALD) non viene accettato poichè sono presenti tre messaggi non ancora letti. 

<p align="center">
  <img src="https://francescotaurino.github.io/MediaRepo/AndroidThingsFirstProject/demo1.gif" width="250"></p>

Un messaggio è descritto dalla semplice classe `Message`:

```Java
￼￼public class Message {
  private final String text;
  private final boolean seen;
  
  public Message() {
    this.text = "";
    this.seen = false;
  }
  
  public Message(String text, boolean seen) {
    this.text = text;
    this.seen = seen;
  }

  public String getText() {
    return this.text;
  }

  public boolean isSeen() {
    return this.seen;
  }
}
```

È importante sottolineare che lo storing persistente dei messaggi non viene effettuato in locale sul dispositivo (ad esempio tramite `SharedPreferences` o `SQLite`) ma in Cloud tramite [Firebase Realtime Database](https://firebase.google.com/docs/database/).
Questa soluzione risolve anche un secondo problema, ovvero la comunicazione tra lo smartphone ed il centralino. Infatti, ogni volta che viene fatta una modifica al database, tutti i client connessi vengono automaticamente notificati in realtime.

Quindi: quando viene digitato ed inviato un nuovo messaggio (**1**), quest'ultimo non viene inserito direttamente nel `RecyclerView`, bensì viene inviato a Firebase Realtime Database, il quale aggiorna il file JSON (**2**). A questo punto, Firebase Realtime Database notifica `PhoneApp` del cambiamento (**3**) e quest'ultima aggiorna la UI, aggiungendo il messaggio appena digitatao nella `RecyclerView` (**4**).
<p align="center">
  <img src="https://francescotaurino.github.io/MediaRepo/AndroidThingsFirstProject/PhoneAppFlow.jpg"></p>

L'invio del messaggio al database avviene all'interno del metodo `send()`, invocato ogni volta che il `Button` viene premuto. 

```Java
public class MainActivity extends AppCompatActivity {
  // Puntatore al database
  private static final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("messages");
  ...
  
  public void send(View view) {  
    String text = editText.getText().toString();
    
    // Se il registro non è pieno
    if (!mainActivityVM.messagesMapIsFull()) {
      // Se il messaggio ha caratteri alfanumerici
      if (text.matches("^[a-zA-Z\\s]+$")) {
        // Aggiorna il database
        dbRef.push().setValue(new Message(text.toUpperCase(), false));
        editText.setText("");
        Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
      }
      else
        Toast.makeText(getApplicationContext(), "Invalid message", Toast.LENGTH_LONG).show();
    }
    else
      Toast.makeText(getApplicationContext(), "Registry full", Toast.LENGTH_LONG).show();
  }
}
```

Il metodo `push()` genera un nuovo ID e `setValue` inserisce effettivamente il messaggio nel database. Alla fine della GIF precedente, la situazione all'interno del database è la seguente:

<p align="center">
  <img src="https://francescotaurino.github.io/MediaRepo/AndroidThingsFirstProject/databasescreen.png"></p>

N.B.: per sempicità di sviluppo dell'applicazione, l'accesso al database è stato reso pubblico.

Appena il database viene modificato, quest'ultimo notifica immediatamente tutti i client in ascolto. Per reagire ai cambiamenti del database:
```Java
public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getName();
  ...
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    ...
    dbRef.addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        // Notifico ViewModel -> Aggiorno Model -> Notifico UI -> Aggiorno RecicylerView
        ...
      }

      @Override
      public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        ...
      }

      @Override
      public void onChildRemoved(DataSnapshot dataSnapshot) {
        ...
      }

      @Override
      public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        ...
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
      }
    });
  }
  ...
}
```

## Applicazione per Android Things: `RaspApp`
Come detto in precedenza, l'applicazione `RaspApp` sfrutta le seguenti periferiche del Rainbow HAT: i tre tasti capacitativi (A, B e C), i corrispondenti led (blu, verde e rosso) e il display alfanumerico.

In particolare:
- i led si accendono alla ricezione di un nuovo messaggio e si spengono quando questo viene visualizzato;
- i tasti sono cliccabili solo se il relativo led è acceso;
- il display si accende e visualizza il messaggio quando un tasto cliccabile viene premuto.

Come prima cosa è necessario accedere ai led e ai tasti.

```Java
public class MainActivity extends AppCompatActivity {
  ...
  private Gpio ledRed;
  private Gpio ledGreen;
  private Gpio ledBlue;

  private Button buttonA;
  private Button buttonB;
  private Button buttonC;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    try {
      ledRed = RainbowHat.openLedRed();
      ledGreen = RainbowHat.openLedGreen();
      ledBlue = RainbowHat.openLedBlue();

      ledRed.setValue(false);
      ledGreen.setValue(false);
      ledBlue.setValue(false);

      buttonA = RainbowHat.openButtonA();
      buttonB = RainbowHat.openButtonB();
      buttonC = RainbowHat.openButtonC();
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException("Unable to open Gpio led or Button");
    }
    ...
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    try {
      if (ledRed != null) {
        ledRed.setValue(false);
        ledRed.close();
      }

      if (ledGreen != null) {
        ledGreen.setValue(false);
        ledGreen.close();
      }

      if (ledBlue != null) {
        ledBlue.setValue(false);
        ledBlue.close();
      }

      if (buttonA != null)
        buttonA.close();

      if (buttonB != null)
        buttonB.close();

      if (buttonC != null)
        buttonC.close();
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException("Unable to close Gpio led or Button");
    }
  }
}
```

Il work flow generale del progetto è il seguente:
<p align="center">
  <img src="https://francescotaurino.github.io/MediaRepo/AndroidThingsFirstProject/RaspAppFlow2.jpg"></p>

Quando il database notifica dei cambiamenti, anche il Raspberry Pi riceve la notifica e, se si tratta di un messaggio appena spedito (ovvero non visualizzato) allora accende il primo led disponibile dei tre (**4**).
A questo punto, il Raspberry Pi aspetta la pressione del tasto da parte dell'utente.

```Java
public class MainActivity extends AppCompatActivity {
  ...
  private static final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("messages");
  ...

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ...
    buttonA.setOnButtonEventListener(new MyOnButtonEventListener(mainActivityVM, dbRef, 0));
    buttonB.setOnButtonEventListener(new MyOnButtonEventListener(mainActivityVM, dbRef, 1));
    buttonC.setOnButtonEventListener(new MyOnButtonEventListener(mainActivityVM, dbRef, 2));
    ...
  }
}

public class MyOnButtonEventListener implements Button.OnButtonEventListener {
  ...

  @Override
  public void onButtonEvent(Button button, boolean pressed) {
    // Se sto premendo ed led del tasto è acceso
    if (pressed && mainActivityVM.slotIsActive(this.button)) {
      Pair<String, Message> p = mainActivityVM.getLiveDataSlot(this.button).getValue();
      // Aggiorno il database con Visualizzato
      dbRef.child(String.format("%s/seen", p.first)).setValue(true);
      // Visualizzo il messaggio sul display
      new MyAsyncTask().execute(p.second.getText());
    }
  }  
}
```

Quando un tasto cliccabile viene premuto dall'utente (**5**), succedono due cose: la visulizzazione del messaggio sul display, che avviene in maniera asincrona tramite un `AsyncTask` (**6**); e la comunicazione al database dell'avvenuta visualizzazione del messaggio (**6**).

```Java
public class MyAsyncTask extends AsyncTask<String, Void, Void> {
  @Override
  protected Void doInBackground(String... texts) {
    String text = texts[0];
    
    try {
      AlphanumericDisplay display = RainbowHat.openDisplay();
      display.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
      display.setEnabled(true);

      display.display(text);
      Thread.sleep(2000);

      // Se il messaggio è più lungo di 4 caratteri bisogna scorrerlo sullo schermo
      if (text.length() > 4) {
        for (int i = 0; i < text.length() - 4; i++) {
          display.display(text.substring(i+1));
          Thread.sleep(1000);
        }
      }

      Thread.sleep(2000);  
      display.clear();
      display.setEnabled(false);
      display.close();
    } 
    catch (IOException | InterruptedException e) {
      e.printStackTrace();
      throw new IllegalStateException("Unable to show message on screen");
    }
    return null;
  }  
}
```

Quindi, il database aggiorna nuovamente il file JSON (**7**) e notifica tutti i client in ascolto (**8**). In particolare a raccogliere questa notifica è `PhoneApp`, la quale aggiorna la UI, spuntando di colore verde il messaggio appena visualizzato sul Raspberry Pi (**9**).

In definitiva, la GIF seguente mostra il comportamento complessivo delle due applicazioni.

<p align="center">
  <img src="https://francescotaurino.github.io/MediaRepo/AndroidThingsFirstProject/final2.gif"></p>
