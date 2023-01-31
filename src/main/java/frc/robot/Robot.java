
package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
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
  private final XboxController m_driverController = new XboxController(0);
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_leftMotor, m_rightMotor);
  private final DoubleSolenoid Solenoid = new DoubleSolenoid(1, PneumaticsModuleType.CTREPCM, 4, 5); //Need to ensure ports are correct
  private final Compressor pcmCompressor = new Compressor(1, PneumaticsModuleType.CTREPCM); //Ensure port 

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_rightMotor.setInverted(true); //  Inverts motor
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
  
    m_robotContainer = new RobotContainer();
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
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() 
  {

  }

  /** This autonomous runs the autonomous command selected by your {@link RobotContainer} class. */
  @Override
  public void autonomousInit() //Runs when autonomous mode begins 
   {

    m_autonomousCommand = m_robotContainer.getAutonomousCommand();

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }
  }
  @Override
  public void autonomousPeriodic() //Autonomous mode
  {  
    //This is where the autonomous code goes 
  }

  @Override
  public void teleopInit() //Runs when operator mode begins
  {

    if (m_autonomousCommand != null) 
    {
      m_autonomousCommand.cancel(); //If autonomous is running (NOT null) then cancel
    }
    pcmCompressor.disable(); //Since compressor automatically turns on when teleop enabled -- disable 
    Solenoid.set(Value.kOff); 
  }

  @Override
  public void teleopPeriodic() //Operator mode func
  {
   
   m_robotDrive.tankDrive(-m_driverController.getLeftY(), m_driverController.getRightY());

   if(m_driverController.getBButtonPressed()) //Does same thing as clicking left and right bumpers 
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
    if(m_driverController.getXButtonPressed())//Reset solenoid
    {
      Solenoid.set(Value.kOff);
    }
    if(m_driverController.getAButtonPressed()) //Toggle compressor 
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
  }

  @Override
  public void testPeriodic() //Test mode func -- this mode kinda dumb 
  {

  }
}
