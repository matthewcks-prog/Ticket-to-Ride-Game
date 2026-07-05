package ttrlondon.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.FaceUpDisplay;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.infrastructure.random.FixedOrderShuffleStrategy;

/** Tests transportation deck and face-up display card supply rules. */
final class CardDeckAndFaceUpDisplayTest {
  @Test
  void deckDrawsFromTopDiscardsAndReshufflesWhenEmpty() {
    TransportCardDeck deck =
        new TransportCardDeck(List.of(CardColor.BLUE), new FixedOrderShuffleStrategy());

    assertEquals(CardColor.BLUE, deck.draw().orElseThrow());
    assertTrue(deck.draw().isEmpty());

    deck.discard(List.of(CardColor.GREEN, CardColor.BLACK));

    assertEquals(CardColor.GREEN, deck.draw().orElseThrow());
    assertEquals(CardColor.BLACK, deck.draw().orElseThrow());
    assertTrue(deck.draw().isEmpty());
    assertEquals(0, deck.discardPileSize());
  }

  @Test
  void takingFaceUpCardRefillsTheSameSlotFromDeck() {
    FaceUpDisplay display =
        new FaceUpDisplay(
            List.of(
                CardColor.BLUE,
                CardColor.GREEN,
                CardColor.BLACK,
                CardColor.PINK,
                CardColor.YELLOW));
    TransportCardDeck deck =
        new TransportCardDeck(List.of(CardColor.ORANGE), new FixedOrderShuffleStrategy());

    assertEquals(CardColor.GREEN, display.take(1));
    display.refillSlot(1, deck);

    assertEquals(
        List.of(
            CardColor.BLUE, CardColor.ORANGE, CardColor.BLACK, CardColor.PINK, CardColor.YELLOW),
        display.visibleCards());
  }

  @Test
  void busFlushDiscardsAllVisibleCardsAndReplacesThem() {
    FaceUpDisplay display =
        new FaceUpDisplay(
            List.of(
                CardColor.BUS,
                CardColor.BUS,
                CardColor.BUS,
                CardColor.PINK,
                CardColor.YELLOW));
    TransportCardDeck deck =
        new TransportCardDeck(
            List.of(
                CardColor.BLUE,
                CardColor.GREEN,
                CardColor.BLACK,
                CardColor.PINK,
                CardColor.YELLOW),
            new FixedOrderShuffleStrategy());

    display.enforceBusFlush(deck);

    assertEquals(
        List.of(
            CardColor.BLUE, CardColor.GREEN, CardColor.BLACK, CardColor.PINK, CardColor.YELLOW),
        display.visibleCards());
    assertEquals(5, deck.discardPileSize());
    assertEquals(0, display.busCount());
  }

  @Test
  void busFlushRepeatsWhenReplacementAlsoContainsThreeBusCards() {
    FaceUpDisplay display =
        new FaceUpDisplay(
            List.of(
                CardColor.BUS,
                CardColor.BUS,
                CardColor.BUS,
                CardColor.PINK,
                CardColor.YELLOW));
    TransportCardDeck deck =
        new TransportCardDeck(
            List.of(
                CardColor.BUS,
                CardColor.BUS,
                CardColor.BUS,
                CardColor.BLUE,
                CardColor.GREEN,
                CardColor.BLACK,
                CardColor.PINK,
                CardColor.YELLOW,
                CardColor.ORANGE,
                CardColor.BLUE),
            new FixedOrderShuffleStrategy());

    display.enforceBusFlush(deck);

    assertEquals(
        List.of(
            CardColor.BLACK, CardColor.PINK, CardColor.YELLOW, CardColor.ORANGE, CardColor.BLUE),
        display.visibleCards());
    assertEquals(10, deck.discardPileSize());
    assertEquals(0, display.busCount());
  }

  @Test
  void busFlushCanReshuffleFlushedCardsWhenDrawPileIsShort() {
    FaceUpDisplay display =
        new FaceUpDisplay(
            List.of(
                CardColor.BUS,
                CardColor.BUS,
                CardColor.BUS,
                CardColor.PINK,
                CardColor.YELLOW));
    TransportCardDeck deck =
        new TransportCardDeck(
            List.of(CardColor.BLUE, CardColor.GREEN), new FixedOrderShuffleStrategy());

    display.enforceBusFlush(deck);

    assertEquals(
        List.of(CardColor.PINK, CardColor.YELLOW, CardColor.BLUE, CardColor.GREEN, CardColor.BUS),
        display.visibleCards());
    assertEquals(0, deck.discardPileSize());
    assertEquals(2, deck.drawPileSize());
  }

  @Test
  void busFlushStopsWhenSupplyCannotProduceAValidDisplay() {
    FaceUpDisplay display =
        new FaceUpDisplay(
            List.of(CardColor.BUS, CardColor.BUS, CardColor.BUS, CardColor.BUS, CardColor.BUS));
    TransportCardDeck deck = new TransportCardDeck(List.of(), new FixedOrderShuffleStrategy());

    display.enforceBusFlush(deck);

    assertEquals(
        List.of(CardColor.BUS, CardColor.BUS, CardColor.BUS, CardColor.BUS, CardColor.BUS),
        display.visibleCards());
  }

  @Test
  void canDrawReflectsDrawAndDiscardPileContents() {
    TransportCardDeck deck =
        new TransportCardDeck(List.of(CardColor.BLUE), new FixedOrderShuffleStrategy());

    assertTrue(deck.canDraw());
    deck.draw();
    assertFalse(deck.canDraw());

    deck.discard(List.of(CardColor.GREEN));
    assertTrue(deck.canDraw());
  }

  @Test
  void isBusAtChecksSlotContentsAndBounds() {
    FaceUpDisplay display =
        new FaceUpDisplay(
            List.of(
                CardColor.BUS,
                CardColor.GREEN,
                CardColor.BLACK,
                CardColor.PINK,
                CardColor.YELLOW));

    assertTrue(display.isBusAt(0));
    assertFalse(display.isBusAt(1));
    assertFalse(display.isBusAt(-1));
    assertFalse(display.isBusAt(5));
  }

  @Test
  void hasNonBusCardOutsideSlotIgnoresExcludedSlotAndBusCards() {
    FaceUpDisplay allBusButOne =
        new FaceUpDisplay(
            List.of(
                CardColor.BUS, CardColor.BUS, CardColor.BUS, CardColor.BUS, CardColor.GREEN));

    assertTrue(allBusButOne.hasNonBusCardOutsideSlot(0));
    assertFalse(allBusButOne.hasNonBusCardOutsideSlot(4));

    FaceUpDisplay allBus =
        new FaceUpDisplay(
            List.of(CardColor.BUS, CardColor.BUS, CardColor.BUS, CardColor.BUS, CardColor.BUS));
    assertFalse(allBus.hasNonBusCardOutsideSlot(-1));
  }
}
