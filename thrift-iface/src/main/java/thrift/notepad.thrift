service com.wjc.iface.thrift.Notepad{
    bool writeToServer(1: string content),
    string readFromServer(1: string fileName)
}