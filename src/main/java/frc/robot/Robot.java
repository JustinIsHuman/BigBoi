
package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.PWMVictorSPX;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
//import edu.wpi.first.wpilibj.DoubleSolenoid.Value.*;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;



/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  //Robot uses PWMVictorSPX motor controllers -- as of 2023
  
   //0 and 1 describe ports -- whatever port which motor is connected to (either 0 or 1)
   //0: Right
   //1: Left


  private Command m_autonomousCommand;
  private RobotContainer m_robotContainer;
  private final MotorController m_leftMotor = new PWMVictorSPX(1);
  private final MotorController m_rightMotor = new PWMVictorSPX(0);
  private final MotorController m_arm = new PWMVictorSPX(2);
  private final XboxController m_driverController = new XboxController(0);
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_leftMotor, m_rightMotor);
  private final DoubleSolenoid Solenoid = new DoubleSolenoid(1, PneumaticsModuleType.CTREPCM, 4, 5); //Need to ensure ports are correct
  private final Compressor pcmCompressor = new Compressor(1, PneumaticsModuleType.CTREPCM); //Ensure port 
  ADXRS450_Gyro gyro = new ADXRS450_Gyro();
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    gyro.calibrate();

    m_rightMotor.setInverted(true); //  Inverts motor
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();
   // CameraServer.startAutomaticCapture();
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    CommandScheduler.getInstance().run();
  }

  /** This function is called once each time the robot enters Disabled mode. */
  @Override
  public void disabledInit()
  {
    
  }

  @Override
  public void disabledPeriodic() 
  {

  }

  /** This autonomous runs the autonomous command selected by your {@link RobotContainer} class. */
  double speed = .5;
  @Override
  public void autonomousInit() //Runs when autonomous mode begins 
   {
    speed = .5;

    m_autonomousCommand = m_robotContainer.getAutonomousCommand();
    

    //Test Code
    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }
    pcmCompressor.disable();
    m_robotDrive.setSafetyEnabled(false);
    gyro.reset();
    Timer.delay(5);
    double axis = gyro.getAngle();
    while(axis < 12.0 && axis > -12.0)
    {
      axis = gyro.getAngle();
      axis = Math.round(axis);
      m_robotDrive.tankDrive(-.6, .6);  
    }
    m_robotDrive.tankDrive(0, 0);    
    System.out.println("hit");
    
  }
  @Override
  public void autonomousPeriodic() //Autonomous mode
  {  

    //This is where the autonomous code goes 
    //Auto Balance
      double axis = gyro.getAngle();
      axis = Math.round((axis));
      System.out.println(axis);
      SmartDashboard.putNumber("Gyro", axis);

      if(axis > 0.0)//Forwards
      {
        m_robotDrive.tankDrive(speed, -speed);
      }
      else if (axis < 0.0)//Backwards
      {
        m_robotDrive.tankDrive(-speed, speed);    
      }
      else//If flat
      {
         m_robotDrive.tankDrive(0, 0);
         //Timer.delay(5);
         speed = speed * .8;
         System.out.println("flat");   
      }    
  }
  int mode = 0;
  int speed1 = 3;


  @Override
  public void teleopInit() //Runs when operator mode begins
  {

    if (m_autonomousCommand != null) 
    {
      m_autonomousCommand.cancel(); //If autonomous is running (NOT null) then cancel
    }
    pcmCompressor.disable(); //Since compressor automatically turns on when teleop enabled -- disable 
    Solenoid.set(Value.kOff); 
    m_robotDrive.setSafetyEnabled(true);
   // gyro.calibrate();

  }

  @Override
  public void teleopPeriodic() //Operator mode func
   {
  
      double axis = gyro.getAngle();
      axis = Math.round((axis));
      System.out.println(axis);
      System.out.flush();

      if(m_driverController.getRightStickButtonPressed())
      {
        m_arm.set(.2);
        Timer.delay(.5);
        m_arm.set(0);

      }
      if(m_driverController.getLeftStickButtonPressed())
      {
        m_arm.set(-.2);
        Timer.delay(.5);
        m_arm.set(0);
      }
      //Accuracy mode -- (Code untested) Possibly control an LED stip with speed modes?
      if(m_driverController.getYButtonPressed())//Drive mode toggle
      { 
        System.out.println("Y press; mode: " + mode); 
        if(mode == 0)
        {
          mode = 1;
        }
        else
        {
          mode = 0;
        }
      }
      if(m_driverController.getXButtonPressed()) // Speed mode toggle
      {
        if(speed1 == 3)
        {
          speed1 = 0;
        }
        else if(speed1 == 0)
        {
          speed1 = 1;
        } else if (speed1 == 1)
        {
          speed1 = 2;
        } else if(speed1 == 2)
        {
          speed1 = 3;
        }
      }
      if(speed1 == 0) //Speed modes
      {
        m_robotDrive.setMaxOutput(.25);
        System.out.println("SuperSlow");

      } else if(speed1 == 1)
      {
        m_robotDrive.setMaxOutput(.50);
        System.out.println("Medium");
      }
      else
      {
        m_robotDrive.setMaxOutput(1);
        System.out.println("Full Speed");
      }
    if(mode == 1) //Arcade mode
    { 
      
      double LeftSpeed = m_driverController.getRightX();
      double RightSpeed = m_driverController.getLeftY();
      m_robotDrive.arcadeDrive(LeftSpeed, RightSpeed); // Uses arcade drive
    }
    else //Tank mode
    {  
       m_robotDrive.tankDrive(-m_driverController.getLeftY(), m_driverController.getRightY());
    }
    if(m_driverController.getAButtonPressed()) //Does same thing as clicking left and right bumpers 
    {
     Solenoid.toggle();
    }
     if(m_driverController.getRightBumperPressed())//Solenoid shi
     {
       Solenoid.set(Value.kForward);
     }
      if(m_driverController.getLeftBumperPressed())//Solenoid shi
     {
       Solenoid.set(Value.kReverse);
     }
  
    if(m_driverController.getBButtonPressed()) //Toggle compressor 
    {
      if(pcmCompressor.isEnabled())
      {
        pcmCompressor.disable();
      }
      else
      {
        pcmCompressor.enableDigital();
      }
      //pcmCompressor.enableDigital(); //Compressor turns on when *pressure switch* indicates system is not full
     // Colin.toggle();
    }
    
  }

  @Override
  public void testInit() //Runs when test mode begins
  {  
    
    CommandScheduler.getInstance().cancelAll();//Cancels everything when running test mode

    pcmCompressor.disable();
    gyro.reset();
    
  }

  @Override
  public void testPeriodic() //Test mode func -- this mode kinda dumb 
  {

    double axis = gyro.getAngle();
    axis = Math.round((axis));
    System.out.println(axis);
    
    
    SmartDashboard.putNumber("Gyro", axis);
    

  }
}
