using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Net;
using System.Net.Sockets;
using System.Windows.Forms;

namespace UDP7Hack
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
           
        }

        int i = 0;
        public void serverThread()
        {
            UdpClient udpClient = new UdpClient(3333);
            while (true)
            {
                IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any,3333);
                Byte[] receiveBytes = udpClient.Receive(ref RemoteIpEndPoint);
                //string returnData = Encoding.ASCII.GetString(receiveBytes);
                    if (receiveBytes[0] == 0x41)
                    {
                        SendKeys.SendWait("{UP}");
                    }
                    else if (receiveBytes[0] == 0x42)
                    {
                        SendKeys.SendWait("{DOWN}");
                    }
    
                //listBox1.Items.Add(RemoteIpEndPoint.Address.ToString() + ":" + receiveBytes[0].ToString());
            }
        }

        private void Form1_Load_1(object sender, EventArgs e)
        {
            Thread thdUDPServer = new Thread(new ThreadStart(serverThread));
            thdUDPServer.Start();
        }
    }
}


