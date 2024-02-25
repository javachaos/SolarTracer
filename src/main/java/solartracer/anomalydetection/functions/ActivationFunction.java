/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package solartracer.anomalydetection.functions;

/**
 * Defines an activation function.
 *
 */
public interface ActivationFunction {

    /**
     * Returns the output of the
     * derivative of the activation function.
     *
     * @param v the input value to the activation function
     * @return the output of the derivative of this activation function
     */
    double derivative(double v);

    /**
     * Activate using the activation function.
     *
     * @param v
     *      input value for this ActivationFunction
     * @return
     *      output value of the function
     */
    double activate(final double v);

    @Override
    int hashCode();

    @Override
    boolean equals(Object o);
}
