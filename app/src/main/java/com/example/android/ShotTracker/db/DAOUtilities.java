package com.example.android.ShotTracker.db;

import android.content.Context;

import com.example.android.ShotTracker.objects.Course;
import com.example.android.ShotTracker.objects.CourseHole;
import com.example.android.ShotTracker.objects.Player;
import com.example.android.ShotTracker.objects.Round;
import com.example.android.ShotTracker.objects.RoundHole;
import com.example.android.ShotTracker.objects.Shot;
import com.example.android.ShotTracker.objects.SubRound;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by damcglinchey on 3/25/15.
 */
public class DAOUtilities {

    private Context mContext = null;

    public DAOUtilities(Context context){ mContext = context; }


    /**
     * Get the average round score for a given player
     * Adjusted to 18 hole par 72 course
     * @param player
     * @return
     */
    public float getAverageAdjustedScorePlayer(Player player) {

        // setup variables & DAO's
        float totScore = 0;
        int Nrounds = 0;

        RoundDAO roundDAO = new RoundDAO(mContext);
        SubRoundDAO subRoundDAO = new SubRoundDAO(mContext);


        // first get a list of rounds
        List<Round> rounds = roundDAO.readListofRounds(player);
//        List<Round> rounds = readListofRounds(player);

        // for each round get a list of subrounds
        for (Round round : rounds) {
            List<SubRound> subRounds = subRoundDAO.readListofSubRounds(round);
            for (SubRound subRound : subRounds) {
                totScore += getAdjustedScoreSubRound(subRound, player);
                Nrounds++;
            }
        }

        if (Nrounds > 0)
            return totScore / (float) Nrounds * 2;
        else
            return 0;
    }

    /**
     * Get average adjusted score given a player and course
     * @param player
     * @param course
     * @return
     */
    public float getAverageAdjustedScorePlayer(Player player, Course course) {

        // setup variables & DAO's
        float totScore = 0;
        int Nrounds = 0;

        RoundDAO roundDAO = new RoundDAO(mContext);
        SubRoundDAO subRoundDAO = new SubRoundDAO(mContext);


        // first get a list of rounds
//        List<Round> rounds = roundDAO.readListofRounds(player);
        List<Round> rounds = roundDAO.readListofRounds(player, course);

        // for each round get a list of subrounds
        for (Round round : rounds) {
            List<SubRound> subRounds = subRoundDAO.readListofSubRounds(round);
            for (SubRound subRound : subRounds) {
                totScore += getAdjustedScoreSubRound(subRound, player);
                Nrounds++;
            }
        }

        if (Nrounds > 0)
            return totScore / (float) Nrounds * 2;
        else
            return 0;
    }

    /**
     * Get the average adjusted score for the front 9 given a player.
     * Adjusted to 9 hole par 36
     * @param player
     * @return
     */
    public float getAverageAdjustedFrontNineScorePlayer(Player player) {
        // setup variables & DAO's
        float totScore = 0;
        int Nrounds = 0;

        RoundDAO roundDAO = new RoundDAO(mContext);
        SubRoundDAO subRoundDAO = new SubRoundDAO(mContext);


        // first get a list of rounds
//        List<Round> rounds = roundDAO.readListofRounds(player);
        List<Round> rounds = readListofRounds(player);

        // for each round get a list of subrounds
        for (Round round : rounds) {
            List<SubRound> subRounds = subRoundDAO.readListofSubRounds(round);
            if (subRounds.size() > 0) {
                totScore += getAdjustedScoreSubRound(subRounds.get(0), player);
                Nrounds++;
            }
        }

        if (Nrounds > 0)
            return totScore / (float) Nrounds;
        else
            return 0;
    }

    /**
     * Get the average adjusted score for the first 9 given a player and course
     * @param player
     * @param course
     * @return
     */
    public float getAverageAdjustedFrontNineScorePlayer(Player player, Course course) {
        // setup variables & DAO's
        float totScore = 0;
        int Nrounds = 0;

        RoundDAO roundDAO = new RoundDAO(mContext);
        SubRoundDAO subRoundDAO = new SubRoundDAO(mContext);


        // first get a list of rounds
        List<Round> rounds = roundDAO.readListofRounds(player, course);
//        List<Round> rounds = readListofRounds(player);

        // for each round get a list of subrounds
        for (Round round : rounds) {
            List<SubRound> subRounds = subRoundDAO.readListofSubRounds(round);
            if (subRounds.size() > 0) {
                totScore += getAdjustedScoreSubRound(subRounds.get(0), player);
                Nrounds++;
            }
        }

        if (Nrounds > 0)
            return totScore / (float) Nrounds;
        else
            return 0;
    }

    /**
     * Get the average adjusted score for the front 9 given a player.
     * Adjusted to 9 hole par 36
     * @param player
     * @return
     */
    public float getAverageAdjustedBackNineScorePlayer(Player player) {
        // setup variables & DAO's
        float totScore = 0;
        int Nrounds = 0;

        RoundDAO roundDAO = new RoundDAO(mContext);
        SubRoundDAO subRoundDAO = new SubRoundDAO(mContext);


        // first get a list of rounds
//        List<Round> rounds = roundDAO.readListofRounds(player);
        List<Round> rounds = readListofRounds(player);

        // for each round get a list of subrounds
        for (Round round : rounds) {
            List<SubRound> subRounds = subRoundDAO.readListofSubRounds(round);
            if (subRounds.size() > 1) {
                totScore += getAdjustedScoreSubRound(subRounds.get(1), player);
                Nrounds++;
            }
        }

        if (Nrounds > 0)
            return totScore / (float) Nrounds;
        else
            return 0;
    }

    /**
     * Get the average adjusted score for the second 9 in a round given a player and course
     * @param player
     * @param course
     * @return
     */
    public float getAverageAdjustedBackNineScorePlayer(Player player, Course course) {
        // setup variables & DAO's
        float totScore = 0;
        int Nrounds = 0;

        RoundDAO roundDAO = new RoundDAO(mContext);
        SubRoundDAO subRoundDAO = new SubRoundDAO(mContext);


        // first get a list of rounds
        List<Round> rounds = roundDAO.readListofRounds(player, course);
//        List<Round> rounds = readListofRounds(player);

        // for each round get a list of subrounds
        for (Round round : rounds) {
            List<SubRound> subRounds = subRoundDAO.readListofSubRounds(round);
            if (subRounds.size() > 1) {
                totScore += getAdjustedScoreSubRound(subRounds.get(1), player);
                Nrounds++;
            }
        }

        if (Nrounds > 0)
            return totScore / (float) Nrounds;
        else
            return 0;
    }

    /**
     * Get the adjusted score for a subround
     * @param subRound
     * @return
     */
    public float getAdjustedScoreSubRound(SubRound subRound, Player player) {

        float total = (float)getScoreSubRound(subRound, player);
        float par = (float)getPlayedPar(subRound, player);

        float adj = total / par * 36;

        return adj;
    }

    /**
     * Get the total score for a given subround
     * @param subRound
     * @return
     */
    public int getScoreSubRound(SubRound subRound, Player player) {
        RoundHoleDAO roundHoleDAO = new RoundHoleDAO(mContext);
        List<RoundHole> roundHoles = roundHoleDAO.readListofRoundHoleRoundPlayer(subRound, player);

        int total = 0;
        for (RoundHole roundHole : roundHoles) {
            total += roundHole.getScore();
        }

        return total;
    }
    /**
     *
     * @param subRound
     * @return
     */
    public int getPlayedPar(SubRound subRound, Player player) {

        RoundHoleDAO roundHoleDAO = new RoundHoleDAO(mContext);
        CourseHoleDAO courseHoleDAO = new CourseHoleDAO(mContext);

        List<RoundHole> roundHoles = roundHoleDAO.readListofRoundHoleRoundPlayer(subRound, player);

        int total = 0;
        for (RoundHole roundHole : roundHoles) {
            CourseHole courseHole = courseHoleDAO.readCourseHoleFromID(roundHole.getCourseHoleID());

            total += courseHole.getPar();
        }

        return total;
    }



    public List<Round> readListofRounds(Player player) {
        List<Round> rounds = new ArrayList<Round>();

        RoundDAO roundDAO = new RoundDAO(mContext);
        SubRoundDAO subRoundDAO = new SubRoundDAO(mContext);
        RoundHoleDAO roundHoleDAO = new RoundHoleDAO(mContext);

        List<Long> usedRoundID = new ArrayList<Long>();
        // read a list of all holes played by the given player
        List<RoundHole> roundHoleList = roundHoleDAO.readListofRoundHolePlayer(player);
        for (RoundHole roundHole : roundHoleList) {
            // get the subround
            SubRound subRound = new SubRound();
            subRound.setID(roundHole.getSubRoundID());
            subRound = subRoundDAO.readSubRound(subRound);

            // if we have this already, continue
            if (usedRoundID.contains(new Long(subRound.getRoundID())))
                continue;

            // get the round
            Round round = new Round();
            round.setID(subRound.getRoundID());
            round = roundDAO.readRound(round);
            rounds.add(round);
            usedRoundID.add(new Long(round.getID()));
        }

        return rounds;
    }

    public void deleteRound(Round round){
        RoundDAO roundDAO = new RoundDAO(mContext);
        SubRoundDAO subRoundDAO = new SubRoundDAO(mContext);
        RoundHoleDAO roundHoleDAO = new RoundHoleDAO(mContext);
        ShotDAO shotDAO = new ShotDAO(mContext);
        ShotLinkDAO shotLinkDAO = new ShotLinkDAO(mContext);

        List<SubRound> subRounds = subRoundDAO.readListofSubRounds(round);

        for (SubRound subRound : subRounds){
            List<RoundHole> roundHoles = roundHoleDAO.readListofRoundHoleRound(subRound);

            for (RoundHole roundHole : roundHoles){
                List<Shot> shots = shotDAO.readListofShotsRoundHoleID(roundHole);

                for (Shot shot : shots){
                    shotLinkDAO.deleteAllShotLinksByShot(shot);

                    shotDAO.deleteShot(shot);
                }

                roundHoleDAO.deleteRoundHole(roundHole);
            }

            subRoundDAO.deleteSubRound(subRound);
        }

        roundDAO.deleteRound(round);

    }
    // \todo Delete Round
    // \todo Course from Round
    // \todo Delete Player (and all player ID's)

    // \todo List of Players given RoundID
//    public List<Player> getUniquePlayerListFromRound(Round round) {
//
//        //first get a list of all subrounds given the round
//        SubRoundDAO subRoundDAO = new SubRoundDAO(context);
//        List<SubRound> subRounds = subRoundDAO.readListofSubRoundsRound(round);
//
//        //for each subround, get a list of unique players
//        List<List<Player>> = new ArrayList<ArrayList<Player>>();
//
//
//    }

}
