import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.regex.*;

public class AcceptClient extends Thread {
    private ServerSocket server;
    private List<ClientData> dataOutputStreams;
    private List<Client> clientList;
    private Map<Integer, SemiClient> semiClientMap = new HashMap<Integer, SemiClient>(  );

    private int numberOfClients = 20;

    public static final Pattern pattern = Pattern.compile( "\\*{3}\\d{1,2};\\d+\\.\\d+;\\d+\\.\\d+\\*{3}");

    public AcceptClient() {
        dataOutputStreams = new LinkedList<ClientData>(  );
        clientList = Collections.synchronizedList( new LinkedList<Client>(  ) );

        try {
            server = new ServerSocket( Code.PORT );
        } catch ( IOException ex ) {
            System.out.print( ex.getMessage() + " sdff" );
        }
    }

    public static void main(String[] args) {
        new AcceptClient().start();
        //acceptClient.start();
    }

    @Override
    public void run () {
        System.out.println( "Server runs" );
        boolean done = true;
        while ( done ) {
            try {
                Socket socket = server.accept();
                System.out.println( "New client connected: " +  socket );

                new SemiClient( socket );
            } catch ( IOException ex ) {
                done = false;
                System.out.print( ex.getMessage() );
            }
        }
    }

    public class SemiClient extends Thread {
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private int number;
        private Player player = null;

        public SemiClient( Socket socket ) {
            try {
                number = numberOfClients;

                dataInputStream = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );
                dataOutputStream =  new DataOutputStream( socket.getOutputStream() );
                //количество клиентов увеличивается
                numberOfClients++;
                this.start();
            } catch ( IOException ioEx ) {
                System.out.println( "Can't connect" );
            }
        }

        //( String code, String name, int HP, float x, float y, float angle )

        public int getNumber() {
            return number;
        }

        public Player getPlayer() {
            return player;
        }

        public void send( String str ) throws IOException {
            try {
                dataOutputStream.writeUTF( str );
            } catch ( IOException e ) {
                throw new IOException( "Невозможно отправить список пользователей!" );
            }
        }

        private void sendAllShipsData() throws IOException {
            StringBuilder sb = new StringBuilder(  );
            sb.append( Code.ENTER_NEW_USER ).append( ";" );
            for ( Map.Entry<Integer, SemiClient> entry : semiClientMap.entrySet() ) {
                StringBuilder stringBuilder = new StringBuilder(  );
                stringBuilder.append( sb ).append( entry.getValue().getNumber() ).append( ";" ).append( entry.getValue().getPlayer().toString() );
                send( stringBuilder.toString() );

                StringBuilder sbb = new StringBuilder(  );
                sbb.append( sb ).append( number ).append( ";" ).append( player.toString() );
                entry.getValue().send( sbb.toString() );
            }
        }

        private void sendTo( String code, String message ) throws IOException {
            StringBuilder sb = new StringBuilder(  );
            sb.append( code ).append( ";" );
            for ( Map.Entry<Integer, SemiClient> entry : semiClientMap.entrySet() ) {
                if ( entry.getValue().getNumber() != number ) {
                    StringBuilder stringBuilder = new StringBuilder(  );
                    stringBuilder.append( sb ).append( number ).append( ";" ).append( message );
                    entry.getValue().send( stringBuilder.toString() );
                }
            }
        }

        private void sendTo ( String message ) throws IOException {
            for ( Map.Entry<Integer, SemiClient> entry : semiClientMap.entrySet() ) {
                if ( entry.getValue().getNumber() != number ) {
                    entry.getValue().send( message );
                }
            }
        }

        @Override
        public void run() {
            boolean done = true;
            while ( done ) {
                try {
                    String line = dataInputStream.readUTF();
                    String[] splitLine = line.split( ";" );
                     if ( splitLine[0].equals( Code.ENTER_NEW_USER ) ) {
                         player = new Player( Float.parseFloat( splitLine[1] ), //x
                                 Float.parseFloat( splitLine[2] ),    //y
                                 Float.parseFloat( splitLine[3] ),    //angle
                                 splitLine[4],                        //Name
                                 Integer.parseInt( splitLine[5] ));   //HP

                         sendAllShipsData();
                         semiClientMap.put( number, this );
                     }
                     if ( splitLine[0].equals( Code.SEND_COORDINATES ) ) {
                        semiClientMap.get( number ).player.changeCoordinates( Float.parseFloat( splitLine[1] ),
                                Float.parseFloat( splitLine[2] ),
                                Float.parseFloat( splitLine[3] ) );

                         sendTo( Code.SEND_COORDINATES, player.getCoordinates() );
                     }
                    if ( splitLine[0].equals( Code.SEND_SHELL ) || splitLine[0].equals( Code.SEND_LASER ) ) {
                        StringBuilder sb = new StringBuilder(  );
                        sb.append( line ).append( ";" ).append( number );
                        sendTo( sb.toString() );
                    }
                } catch ( IOException ioEx ) {
                    done = false;
                    try {
                        sendTo( Code.EXIT_USER, "" );
                        dataOutputStream.close();
                        dataInputStream.close();
                        semiClientMap.remove( number );
                    } catch ( IOException iEx ) {
                        System.out.println( "Can't close data input and output streams" );
                    }
                    System.out.println( "Can't read data" );

                }
            }
        }
    }
}
