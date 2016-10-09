package theAccountant;

import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Save data, kill intruders!
 **/
class Player {

    public static void main(String args[]) {
    	Wolff wolff = new Wolff();
    	ArrayList<Enemy> enemyAL = new ArrayList<Enemy>();
    	ArrayList<DataPoint> dataPointAL = new ArrayList<DataPoint>();
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
        	  int[] wolffPosition = new int[2];
        	  wolffPosition[0] = in.nextInt();
              wolffPosition[1] = in.nextInt();
              wolff.setPosition(wolffPosition[0], wolffPosition[1]);
              
           
              int dataPointCount = in.nextInt();
              int[] dataPointPositionArray = new int[2];
              for (int i = 0; i < dataPointCount; i++) {
              	
                  DataPoint dataPoint = new DataPoint(in.nextInt());
                  dataPointPositionArray[0] = in.nextInt();
                  dataPointPositionArray[1]= in.nextInt();
                  dataPoint.setPosition(dataPointPositionArray[0], dataPointPositionArray[1]);
                  dataPointAL.add(dataPoint);
              }
              
            int enemyCount = in.nextInt();
            for (int i = 0; i < enemyCount; i++) {
            	
            	int[] enemyPositionArray = new int[2];
            	Enemy enemy = new Enemy(in.nextInt());
                enemyPositionArray[0] = in.nextInt();
                enemyPositionArray[1] = in.nextInt();
                enemy.setPosition(enemyPositionArray[0], enemyPositionArray[1]);
                enemy.setLife(in.nextInt());
                /*
                enemyPositionArray[0] = in.nextInt(); //next enemy position
                enemyPositionArray[1] = in.nextInt(); // next enemy position
                enemy.setNextPosition(enemyPositionArray[0], enemyPositionArray[1]);
                */
                enemyAL.add(enemy);
            }
            
            for (DataPoint dataPoint : dataPointAL){
				dataPoint.determineDanger(enemyAL); // num of turns until captured
				wolff.isDataPointSavable(dataPoint); 
            }
            
            dataPointAL.sort(new DataPoint.dangerComparator());
            DataPoint saveDataPoint = null;
            for (DataPoint dataPoint: dataPointAL){
	        	if(dataPoint.getCaptured()){// only consider dataPoints that are savable
	        		continue;
	        	}
	        	else{
	        		saveDataPoint = dataPoint;
	        		break;
	        	}
            }
            
            
            // determine action (move or shoot)
            String action = wolff.determineAction(saveDataPoint);
            String wolffActionString = null;
            if(action.equals("MOVE")){
            	wolff.setDestPosition(saveDataPoint.getX(), saveDataPoint.getY());
            	wolffActionString = action + " " + String.valueOf(wolff.destX + " " + wolff.destY);
            }
            else 
            {
            	wolffActionString = action + " " + String.valueOf(saveDataPoint.getClosestEnemy().getId());
            }
            
            
            
            
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            //System.out.println("0 0"); // Your destination coordinates
            System.out.println(wolffActionString); // Your destination coordinates
            enemyAL.clear();
            dataPointAL.clear();
        }
        

        
    }
    
   static class  Wolff {
    	int x;
    	int y;
    	int destX;// coordinates for Wolff to move
    	int destY;// coordinates for Wolff to move
    	static final int movingRange = 1000;
    
    	

    	
         int findDistanceToTarget(int initX, int initY, int targetX, int targetY){
    		return (int) Math.hypot(targetX - initX, targetY - initY);
    	}
    	
    	void setPosition(int NewX, int NewY){
    		x = NewX;
    		y = NewY;
    	}
    	
    	void setDestPosition(int newDestX, int newDestY){
    		destX = newDestX;
    		destY = newDestY;
    	}
    	
    	long findDamageDealt(int initX, int initY, Enemy enemy){
    		int distToEnemy = enemy.findDistanceToTarget(initX, initY, enemy.getX(), enemy.getY());
    		return Math.round((125000/Math.pow(distToEnemy,1.2)));
    	}
    	
    	long findDamageDealt(int initX, int initY, int enemyX,int enemyY){
    		int distToEnemy = findDistanceToTarget(initX, initY, enemyX, enemyY );
    		return Math.round((125000/Math.pow(distToEnemy,1.2)));
    	}
    	
    	int[] findMeanCoordinates(int x1, int y1, int x2, int y2){
    		int meanX = (x2-x1)/2  + x1;
    		int meanY = (y2-y1)/2 + y1;
    		int [] meanCoordinates = {meanX,meanY};
    		return meanCoordinates;
    	}
    	
	   	 double findAngleToTarget(int initX,int initY, int targetX, int targetY){
			 return  Math.atan2(targetY - y, targetX - x);
		 }
    	
	   	 int[] moveTowardTarget(int initX, int initY, int targetX, int targetY){
			 double theta = findAngleToTarget(initX, initY, targetX, targetY);
			 int moveX = (int) (movingRange * Math.cos(theta));
			 int moveY = (int) (movingRange * Math.sin(theta));
			 int[] newCoordinates = {initX + moveX, initY + moveY};
			 return newCoordinates;
		 }
	
    	
   	 boolean isDataPointSavable(DataPoint dataPoint){
		 int turnsTilCapture = dataPoint.closestEnemy.findNumTurnsUntilDataPointCaptured(dataPoint.getX(), dataPoint.getY());
		 int turnsTilKillAlwaysShooting = turnsTilKillAlwaysShooting(dataPoint.getClosestEnemy(), dataPoint);
		 int turnsTil1ShotKill = turnsTill1ShotKill(dataPoint.getClosestEnemy(), dataPoint);
		 if (turnsTilKillAlwaysShooting > turnsTilCapture || turnsTil1ShotKill > turnsTilCapture){
			 dataPoint.setCaptured();
			 return false;
		 }
		 return true;
	 }
	 
	 int turnsTilKillAlwaysShooting(Enemy enemy, DataPoint dataPoint){
		 int enemyLife = enemy.getLife();
		 int enemyX = enemy.getX();
		 int enemyY = enemy.getY();
		 int turnsTilKill = 0;
		 
		 while (enemyLife > 0){
			 int damageDealt = (int) findDamageDealt(x, y, enemyX, enemyY);
			 enemyLife = enemyLife - damageDealt;
			 int[] newEnemyPosition = enemy.moveTowardTarget(enemyX, enemyY, dataPoint.getX(), dataPoint.getY());
			 enemyX = newEnemyPosition[0];
			 enemyY = newEnemyPosition[1];
			 turnsTilKill++;
		 }
		 
		 return turnsTilKill;
	 }
	 
	 int turnsTill1ShotKill (Enemy enemy, DataPoint dataPoint){
		 int enemyLife = enemy.getLife();
		 int enemyX = enemy.getX();
		 int enemyY = enemy.getY();
		 int wolffX = x;
		 int wolffY = y;
		 int distanceNeededForKill = (int) Math.pow(125000/enemyLife, (1/1.2));
		 if(distanceNeededForKill < Enemy.shootRange){
			 System.err.println("Cannot Kill Enemy in 1 Shot!!!!!");
		 }
		 int distToEnemy = findDistanceToTarget(wolffX, wolffY, enemyX, enemyY);
		 int turnTillKill = 0;
		 while (distToEnemy > distanceNeededForKill){
			 int[] wolffTargetCoordinates = findMeanCoordinates(enemyX, enemyY, dataPoint.getX(), dataPoint.getY());
			 int[] wolffNewCoordinates = moveTowardTarget(wolffX, wolffY, wolffTargetCoordinates[0], wolffTargetCoordinates[1]);
			 wolffX = wolffNewCoordinates[0] ;
			 wolffY = wolffNewCoordinates[1];
			 
			 int[] enemyNewCoordinates = enemy.moveTowardTarget(enemyX, enemyY, dataPoint.getX(), dataPoint.getY());
			 enemyX = enemyNewCoordinates[0];
			 enemyY = enemyNewCoordinates[1];
			 distToEnemy = findDistanceToTarget(wolffX, wolffY, enemyX, enemyY);
			 turnTillKill++;
		 }
		 
		 return turnTillKill;
	 }
	 
	 String determineAction(DataPoint dataPoint){
		 int turnsTill1ShotKill = turnsTill1ShotKill(dataPoint.closestEnemy, dataPoint);
		 int turnsTilKillAlwaysShooting = turnsTilKillAlwaysShooting(dataPoint.closestEnemy, dataPoint);
		 String action = null;
		 if(turnsTill1ShotKill-turnsTilKillAlwaysShooting >= 0){
			 action = "MOVE";
		 }
		 else 
		 {
			 action = "SHOOT";
		 }
		 return action;
	 }
    	
    }
    
   static class Enemy {
    	int id;
	   	int x;
    	int y;
    	int nextX;
    	int nextY;
    	int life;
    	static final int movingRange = 400;
    	static final int shootRange = 2000;
    	static final int captureRange = 500;
    	
    	Enemy(int myId){
    		id = myId;
    	}
    	
    	void setPosition(int NewX, int NewY){
    		x = NewX;
    		y = NewY;
    	}
    	
    	void setNextPosition(int NewX, int NewY){
    		nextX = NewX;
    		nextY = NewY;
    	}
    	
    	int getX(){
    		return x;
    	}
    	
    	int getY(){
    		return y;
    	}
    	
    	 int getId(){
    		return id;
    	}
    	
    	int findNumTurnsUntilDataPointCaptured(int targetX,int targetY){
    		int distToTarget = this.findDistanceToTarget(targetX, targetY);
    		int distToTargetInCaptureRange = distToTarget-captureRange;
    		return (int) Math.ceil(distToTargetInCaptureRange/movingRange);
    	}
    	
        int findDistanceToTarget(int initX, int initY, int targetX, int targetY){
   		return (int) Math.hypot(targetX - initX, targetY - initY);
        }
        
        int findDistanceToTarget(int targetX, int targetY){
       		return (int) Math.hypot(targetX - x, targetY - y);
            }
    	 
    	 double findAngleToTarget(int initX, int initY, int targetX, int targetY){
    		 return Math.atan2(targetY - initY, targetX - initX);
    	 }
    	 
	   	 int[] moveTowardTarget(int initX, int initY, int targetX, int targetY){
			 double theta = findAngleToTarget(initX, initY, targetX, targetY);
			 int moveX = (int) (movingRange * Math.cos(theta));
			 int moveY = (int) (movingRange * Math.sin(theta));
			 int[] newCoordinates = {initX + moveX, initY + moveY};
			 return newCoordinates;
		 }
    	
    	 void setLife(int newLife){
    		 life = newLife;
    	 }
    	 
    	 int getLife(){
    		 return life;
    	 }
    	 
    	
    }
   
   static class DataPoint{
	   int x;
	   int y;
	   int id;
	   Enemy closestEnemy;
	   int danger;
	   boolean captured = false; // true if cannot be saved
	   
	   
	   static class dangerComparator implements Comparator<DataPoint>{
		   public int compare(DataPoint dataPoint1, DataPoint dataPoint2){
			   Integer danger1 = dataPoint1.getDanger();
			   Integer danger2 = dataPoint2.getDanger();
			   return danger1.compareTo(danger2);
		   }
	   }
		   
	   
	   DataPoint(int myId){
		   id = myId;
	   }
	   
	   void determineDanger(ArrayList<Enemy> enemyAL){
		   findClosestEnemy(enemyAL);
		   setDanger(closestEnemy.findNumTurnsUntilDataPointCaptured(x, y));
	   }
	   
	   Enemy findClosestEnemy(ArrayList<Enemy> enemyAL){
		   int counter = 0;
		   int minEnemyDist = 0;
		   for (Enemy enemy : enemyAL){
			   int enemyDist = findDistanceToTarget(enemy.getX(),enemy.getY());
			   if (counter == 0 || enemyDist < minEnemyDist){
				   minEnemyDist = enemyDist;
				   setClosestEnemy(enemy);
			   }
			   counter++;
		   }
		   //System.err.println("closestEnemy: " + closestEnemy.getId());
		   return closestEnemy;
	   }
	   
	   void setClosestEnemy(Enemy enemy){
		   closestEnemy = enemy;
	   }
	   
	   Enemy getClosestEnemy(){
		   return closestEnemy;
	   }
	   
	   
	    int findDistanceToTarget(int targetX, int targetY){
   		return (int) Math.hypot(targetX - x, targetY - y);
   		}
	   
	   void setPosition(int newX, int newY){
		   x = newX;
		   y = newY;
	   }
	   
	    void setDanger(int newDanger){
		   danger = newDanger;
	   }
	   
	    int getDanger(){
		   return danger;
	   }
	   
	   int getX(){
		   return x;
	   }
	   
	   int getY(){
		   return y;
	   }
	   
	   void setCaptured(){
		   captured = true;
	   }
	   
	   boolean getCaptured(){
		   return captured;
	   }

   }
    
}
